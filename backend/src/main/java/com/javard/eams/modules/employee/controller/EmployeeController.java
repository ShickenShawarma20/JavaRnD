package com.javard.eams.modules.employee.controller;

import com.javard.eams.common.response.ApiResponse;
import com.javard.eams.common.response.PageResponse;
import com.javard.eams.modules.employee.dto.req.EmployeeCreateRequest;
import com.javard.eams.modules.employee.dto.req.EmployeeUpdateRequest;
import com.javard.eams.modules.employee.dto.res.EmployeeResponse;
import com.javard.eams.modules.employee.entity.EmploymentStatus;
import com.javard.eams.modules.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Endpoints for employee directory, profiling, and hierarchy")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Register new employee and create user account")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Employee registered successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'MANAGER')")
    @Operation(summary = "Search employees with pagination and filters")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<EmployeeResponse> response = employeeService.searchEmployees(search, departmentId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Employees retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed employee information by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable UUID id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee retrieved successfully"));
    }

    @GetMapping("/manager/{managerId}/reports")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'MANAGER')")
    @Operation(summary = "Get list of direct reporting subordinates for a manager")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getDirectReports(@PathVariable UUID managerId) {
        List<EmployeeResponse> response = employeeService.getDirectReports(managerId);
        return ResponseEntity.ok(ApiResponse.success(response, "Reporting employees retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Update employee profile details")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Employee updated successfully"));
    }
}

