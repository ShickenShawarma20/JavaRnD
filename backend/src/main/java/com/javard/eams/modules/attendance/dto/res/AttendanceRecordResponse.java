package com.javard.eams.modules.attendance.dto.res;

import com.javard.eams.modules.attendance.entity.AttendanceSession;
import com.javard.eams.modules.attendance.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRecordResponse {
    private UUID sessionId;
    private UUID employeeId;
    private String employeeCode;
    private String employeeName;
    private String shiftName;
    private LocalDate workDate;
    private Instant firstCheckIn;
    private Instant lastCheckOut;
    private int grossMinutes;
    private int unpaidBreakMinutes;
    private int netWorkMinutes;
    private int overtimeMinutes;
    private boolean lateEntry;
    private int lateEntryMinutes;
    private boolean earlyExit;
    private int earlyExitMinutes;
    private AttendanceStatus status;
    private boolean regularized;

    public static AttendanceRecordResponse from(AttendanceSession session) {
        return AttendanceRecordResponse.builder()
                .sessionId(session.getId())
                .employeeId(session.getEmployee().getId())
                .employeeCode(session.getEmployee().getEmployeeCode())
                .employeeName(session.getEmployee().getFullName())
                .shiftName(session.getShift().getShiftName())
                .workDate(session.getWorkDate())
                .firstCheckIn(session.getFirstCheckIn())
                .lastCheckOut(session.getLastCheckOut())
                .grossMinutes(session.getGrossMinutes())
                .unpaidBreakMinutes(session.getUnpaidBreakMinutes())
                .netWorkMinutes(session.getNetWorkMinutes())
                .overtimeMinutes(session.getOvertimeMinutes())
                .lateEntry(session.isLateEntry())
                .lateEntryMinutes(session.getLateEntryMinutes())
                .earlyExit(session.isEarlyExit())
                .earlyExitMinutes(session.getEarlyExitMinutes())
                .status(session.getStatus())
                .regularized(session.isRegularized())
                .build();
    }
}

