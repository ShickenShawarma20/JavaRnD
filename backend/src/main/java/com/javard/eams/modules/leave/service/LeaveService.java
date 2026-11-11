package com.javard.eams.modules.leave.service;

import com.javard.eams.common.exception.BadRequestException;
import com.javard.eams.common.exception.ResourceNotFoundException;
import com.javard.eams.common.response.PageResponse;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import com.javard.eams.modules.leave.dto.req.LeaveApplyRequest;
import com.javard.eams.modules.leave.dto.res.LeaveResponse;
import com.javard.eams.modules.leave.entity.LeaveBalance;
import com.javard.eams.modules.leave.entity.LeaveRequest;
import com.javard.eams.modules.leave.entity.LeaveStatus;
import com.javard.eams.modules.leave.entity.LeaveType;
import com.javard.eams.modules.leave.repository.LeaveBalanceRepository;
import com.javard.eams.modules.leave.repository.LeaveRequestRepository;
import com.javard.eams.modules.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveResponse applyLeave(UUID employeeId, LeaveApplyRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Leave end date cannot be before start date");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        int currentYear = request.getStartDate().getYear();

        // Check Balance
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, request.getLeaveTypeId(), currentYear)
                .orElseGet(() -> {
                    // Initialize balance if not found
                    LeaveBalance nb = LeaveBalance.builder()
                            .employee(employee)
                            .leaveType(leaveType)
                            .year(currentYear)
                            .totalAllocated(leaveType.getAnnualQuota())
                            .used(BigDecimal.ZERO)
                            .balance(leaveType.getAnnualQuota())
                            .build();
                    return leaveBalanceRepository.save(nb);
                });

        if (balance.getBalance().compareTo(request.getTotalDays()) < 0) {
            throw new BadRequestException("Insufficient leave balance. Available: " + balance.getBalance() + " days");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(request.getTotalDays())
                .halfDay(request.isHalfDay())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return LeaveResponse.from(saved);
    }

    @Transactional
    public LeaveResponse actionLeave(UUID requestId, UUID reviewerEmployeeId, boolean approve, String remarks) {
        LeaveRequest leave = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already " + leave.getStatus());
        }

        Employee reviewer = employeeRepository.findById(reviewerEmployeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer employee not found"));

        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(Instant.now());

        if (approve) {
            leave.setStatus(LeaveStatus.APPROVED);

            // Deduct balance
            int year = leave.getStartDate().getYear();
            leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                    leave.getEmployee().getId(), leave.getLeaveType().getId(), year
            ).ifPresent(balance -> {
                balance.setUsed(balance.getUsed().add(leave.getTotalDays()));
                balance.setBalance(balance.getBalance().subtract(leave.getTotalDays()));
                leaveBalanceRepository.save(balance);
            });
        } else {
            leave.setStatus(LeaveStatus.REJECTED);
            leave.setRejectionRemarks(remarks);
        }

        return LeaveResponse.from(leaveRequestRepository.save(leave));
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> getMyLeaves(UUID employeeId, Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable);
        return PageResponse.from(page.map(LeaveResponse::from));
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingForManager(UUID managerId) {
        return leaveRequestRepository.findByManagerAndStatus(managerId, LeaveStatus.PENDING)
                .stream()
                .map(LeaveResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveBalance> getMyBalances(UUID employeeId, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year);
    }
}

