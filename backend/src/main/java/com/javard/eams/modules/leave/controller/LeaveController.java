package com.javard.eams.modules.leave.controller;

import com.javard.eams.common.response.ApiResponse;
import com.javard.eams.common.response.PageResponse;
import com.javard.eams.modules.auth.entity.User;
import com.javard.eams.modules.auth.repository.UserRepository;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import com.javard.eams.modules.leave.dto.req.LeaveApplyRequest;
import com.javard.eams.modules.leave.dto.res.LeaveResponse;
import com.javard.eams.modules.leave.entity.LeaveBalance;
import com.javard.eams.modules.leave.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Endpoints for leave requests, balances, and manager approvals")
public class LeaveController {

    private final LeaveService leaveService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/apply")
    @Operation(summary = "Submit a new leave application")
    public ResponseEntity<ApiResponse<LeaveResponse>> applyLeave(
            @Valid @RequestBody LeaveApplyRequest request,
            Authentication authentication
    ) {
        Employee employee = getEmployeeFromAuth(authentication);
        LeaveResponse response = leaveService.applyLeave(employee.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Leave application submitted"));
    }

    @GetMapping("/my-requests")
    @Operation(summary = "Get current employee's leave applications")
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> getMyLeaves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Employee employee = getEmployeeFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<LeaveResponse> response = leaveService.getMyLeaves(employee.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Leave requests retrieved"));
    }

    @GetMapping("/balances")
    @Operation(summary = "Get current leave balances for logged-in employee")
    public ResponseEntity<ApiResponse<List<LeaveBalance>>> getMyBalances(
            @RequestParam(required = false) Integer year,
            Authentication authentication
    ) {
        Employee employee = getEmployeeFromAuth(authentication);
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        List<LeaveBalance> balances = leaveService.getMyBalances(employee.getId(), targetYear);
        return ResponseEntity.ok(ApiResponse.success(balances, "Leave balances retrieved"));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Get team leave requests awaiting manager approval")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getPendingLeaves(Authentication authentication) {
        Employee manager = getEmployeeFromAuth(authentication);
        List<LeaveResponse> list = leaveService.getPendingForManager(manager.getId());
        return ResponseEntity.ok(ApiResponse.success(list, "Pending leave applications retrieved"));
    }

    @PutMapping("/{id}/action")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Approve or reject a leave request")
    public ResponseEntity<ApiResponse<LeaveResponse>> actionLeave(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        Employee reviewer = getEmployeeFromAuth(authentication);
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        String remarks = (String) body.get("remarks");

        LeaveResponse response = leaveService.actionLeave(id, reviewer.getId(), approve, remarks);
        return ResponseEntity.ok(ApiResponse.success(response, approve ? "Leave approved" : "Leave rejected"));
    }

    private Employee getEmployeeFromAuth(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No employee profile found"));
    }
}

