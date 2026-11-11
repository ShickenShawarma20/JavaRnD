package com.javard.eams.modules.regularization.service;

import com.javard.eams.common.exception.BadRequestException;
import com.javard.eams.common.exception.ResourceNotFoundException;
import com.javard.eams.modules.attendance.engine.AttendanceCalculationEngine;
import com.javard.eams.modules.attendance.entity.AttendanceSession;
import com.javard.eams.modules.attendance.repository.AttendanceSessionRepository;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import com.javard.eams.modules.regularization.dto.req.RegularizationApplyRequest;
import com.javard.eams.modules.regularization.entity.AttendanceCorrection;
import com.javard.eams.modules.regularization.repository.AttendanceCorrectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegularizationService {

    private final AttendanceCorrectionRepository correctionRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceCalculationEngine calculationEngine;

    @Transactional
    public AttendanceCorrection applyCorrection(UUID employeeId, RegularizationApplyRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        AttendanceSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));

        AttendanceCorrection correction = AttendanceCorrection.builder()
                .session(session)
                .employee(employee)
                .requestedCheckIn(request.getRequestedCheckIn())
                .requestedCheckOut(request.getRequestedCheckOut())
                .reason(request.getReason())
                .status("PENDING")
                .build();

        return correctionRepository.save(correction);
    }

    @Transactional
    public AttendanceCorrection actionCorrection(UUID correctionId, UUID reviewerEmployeeId, boolean approve, String remarks) {
        AttendanceCorrection correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Correction request not found"));

        if (!"PENDING".equalsIgnoreCase(correction.getStatus())) {
            throw new BadRequestException("Request is already processed: " + correction.getStatus());
        }

        Employee reviewer = employeeRepository.findById(reviewerEmployeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        correction.setReviewedBy(reviewer);
        correction.setReviewedAt(Instant.now());

        if (approve) {
            correction.setStatus("APPROVED");

            // Overwrite attendance session punches and recalculate
            AttendanceSession session = correction.getSession();
            if (correction.getRequestedCheckIn() != null) {
                session.setFirstCheckIn(correction.getRequestedCheckIn());
            }
            if (correction.getRequestedCheckOut() != null) {
                session.setLastCheckOut(correction.getRequestedCheckOut());
            }
            session.setRegularized(true);

            calculationEngine.recalculateSession(session);
            sessionRepository.save(session);
        } else {
            correction.setStatus("REJECTED");
            correction.setRejectionReason(remarks);
        }

        return correctionRepository.save(correction);
    }

    @Transactional(readOnly = true)
    public List<AttendanceCorrection> getPendingForManager(UUID managerId) {
        return correctionRepository.findPendingForManager(managerId);
    }

    @Transactional(readOnly = true)
    public List<AttendanceCorrection> getMyCorrections(UUID employeeId) {
        return correctionRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }
}

