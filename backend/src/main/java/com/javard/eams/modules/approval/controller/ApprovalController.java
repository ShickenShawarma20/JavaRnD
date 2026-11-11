package com.javard.eams.modules.approval.controller;

import com.javard.eams.common.response.ApiResponse;
import com.javard.eams.modules.approval.entity.ApprovalRequest;
import com.javard.eams.modules.approval.service.ApprovalWorkflowEngine;
import com.javard.eams.modules.auth.entity.User;
import com.javard.eams.modules.auth.repository.UserRepository;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/approvals")
@RequiredArgsConstructor
@Tag(name = "Unified Approvals", description = "Enterprise inbox consolidating leaves, corrections, and scheduling requests")
public class ApprovalController {

    private final ApprovalWorkflowEngine workflowEngine;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping("/inbox")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Get unified pending approval queue for reviewer")
    public ResponseEntity<ApiResponse<List<ApprovalRequest>>> getInbox(Authentication authentication) {
        Employee reviewer = getEmployeeFromAuth(authentication);
        List<ApprovalRequest> inbox = workflowEngine.getUnifiedInboxForManager(reviewer.getId());
        return ResponseEntity.ok(ApiResponse.success(inbox, "Unified inbox retrieved"));
    }

    @PostMapping("/{id}/action")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Approve, reject, or comment on an approval request")
    public ResponseEntity<ApiResponse<ApprovalRequest>> actionRequest(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        Employee reviewer = getEmployeeFromAuth(authentication);
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        String remarks = (String) body.get("remarks");

        ApprovalRequest result = workflowEngine.processApprovalAction(id, reviewer.getId(), approve, remarks);
        return ResponseEntity.ok(ApiResponse.success(result, approve ? "Request approved" : "Request rejected"));
    }

    private Employee getEmployeeFromAuth(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found"));
    }
}

