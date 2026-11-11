package com.javard.eams.modules.leave.dto.res;

import com.javard.eams.modules.leave.entity.LeaveRequest;
import com.javard.eams.modules.leave.entity.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private String leaveTypeCode;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private boolean halfDay;
    private String reason;
    private LeaveStatus status;
    private String reviewerName;
    private Instant reviewedAt;
    private String rejectionRemarks;
    private Instant createdAt;

    public static LeaveResponse from(LeaveRequest request) {
        String reviewerName = request.getReviewedBy() != null ? request.getReviewedBy().getFullName() : null;
        return LeaveResponse.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getFullName())
                .employeeCode(request.getEmployee().getEmployeeCode())
                .leaveTypeCode(request.getLeaveType().getTypeCode())
                .leaveTypeName(request.getLeaveType().getTypeName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(request.getTotalDays())
                .halfDay(request.isHalfDay())
                .reason(request.getReason())
                .status(request.getStatus())
                .reviewerName(reviewerName)
                .reviewedAt(request.getReviewedAt())
                .rejectionRemarks(request.getRejectionRemarks())
                .createdAt(request.getCreatedAt())
                .build();
    }
}

