package com.javard.eams.modules.regularization.controller;

import com.javard.eams.common.response.ApiResponse;
import com.javard.eams.modules.auth.entity.User;
import com.javard.eams.modules.auth.repository.UserRepository;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import com.javard.eams.modules.regularization.dto.req.RegularizationApplyRequest;
import com.javard.eams.modules.regularization.entity.AttendanceCorrection;
import com.javard.eams.modules.regularization.service.RegularizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/regularizations")
@RequiredArgsConstructor
@Tag(name = "Attendance Regularization", description = "Endpoints for missing punch corrections and manager review")
public class RegularizationController {

    private final RegularizationService regularizationService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @PostMapping
    @Operation(summary = "Apply for missed punch or attendance regularization")
    public ResponseEntity<ApiResponse<AttendanceCorrection>> apply(
            @Valid @RequestBody RegularizationApplyRequest request,
            Authentication authentication) {
        Employee employee = getEmployeeFromAuth(authentication);
        AttendanceCorrection result = regularizationService.applyCorrection(employee.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(result, "Correction request submitted"));
    }

    @GetMapping("/my-requests")
    @Operation(summary = "List all my submitted regularization requests")
    public ResponseEntity<ApiResponse<List<AttendanceCorrection>>> getMyRequests(Authentication authentication) {
        Employee employee = getEmployeeFromAuth(authentication);
        List<AttendanceCorrection> list = regularizationService.getMyCorrections(employee.getId());
        return ResponseEntity.ok(ApiResponse.success(list, "My correction requests"));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Get list of pending regularization requests for manager's subordinates")
    public ResponseEntity<ApiResponse<List<AttendanceCorrection>>> getPendingForManager(Authentication authentication) {
        Employee manager = getEmployeeFromAuth(authentication);
        List<AttendanceCorrection> list = regularizationService.getPendingForManager(manager.getId());
        return ResponseEntity.ok(ApiResponse.success(list, "Pending regularization requests"));
    }

    @PutMapping("/{id}/action")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Approve or reject a regularization request")
    public ResponseEntity<ApiResponse<AttendanceCorrection>> action(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        Employee reviewer = getEmployeeFromAuth(authentication);
        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        String remarks = (String) body.get("remarks");

        AttendanceCorrection result = regularizationService.actionCorrection(id, reviewer.getId(), approve, remarks);
        return ResponseEntity
                .ok(ApiResponse.success(result, approve ? "Regularization approved" : "Regularization rejected"));
    }

    private Employee getEmployeeFromAuth(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No employee profile found"));
    }
}
