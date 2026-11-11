package com.javard.eams.modules.attendance.controller;

import com.javard.eams.common.response.ApiResponse;
import com.javard.eams.common.response.PageResponse;
import com.javard.eams.modules.attendance.dto.req.PunchRequest;
import com.javard.eams.modules.attendance.dto.res.AttendanceRecordResponse;
import com.javard.eams.modules.attendance.entity.AttendanceStatus;
import com.javard.eams.modules.attendance.service.AttendanceService;
import com.javard.eams.modules.auth.entity.User;
import com.javard.eams.modules.auth.repository.UserRepository;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "Endpoints for clocking in/out, breaks, daily sheets, and logs")
public class AttendancePunchController {

    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @PostMapping("/punch")
    @Operation(summary = "Record check-in, check-out, or break punch with server-validated timestamp")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> punch(
            @Valid @RequestBody PunchRequest request,
            Authentication authentication
    ) {
        Employee employee = getEmployeeFromAuth(authentication);
        AttendanceRecordResponse response = attendanceService.processPunch(employee.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Punch recorded successfully"));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's live attendance session and timer stats for logged-in employee")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> getTodaySession(Authentication authentication) {
        Employee employee = getEmployeeFromAuth(authentication);
        AttendanceRecordResponse response = attendanceService.getTodaySession(employee.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Today's session retrieved"));
    }

    @GetMapping("/my-history")
    @Operation(summary = "Get monthly attendance history for calendar display")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getMonthlyHistory(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication
    ) {
        Employee employee = getEmployeeFromAuth(authentication);
        List<AttendanceRecordResponse> response = attendanceService.getMonthlyHistory(employee.getId(), year, month);
        return ResponseEntity.ok(ApiResponse.success(response, "Monthly history retrieved"));
    }

    @GetMapping("/daily-sheet")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Daily company-wide attendance sheet with filters")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceRecordResponse>>> getDailySheet(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<AttendanceRecordResponse> response = attendanceService.getDailySheet(targetDate, departmentId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Daily sheet retrieved"));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPER_ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Get attendance for manager's direct reports")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getTeamAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        Employee manager = getEmployeeFromAuth(authentication);
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<AttendanceRecordResponse> response = attendanceService.getTeamAttendance(manager.getId(), targetDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Team attendance retrieved"));
    }

    private Employee getEmployeeFromAuth(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No employee profile found for user"));
    }
}

