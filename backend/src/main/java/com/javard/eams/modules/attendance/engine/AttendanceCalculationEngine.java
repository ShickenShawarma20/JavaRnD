package com.javard.eams.modules.attendance.engine;

import com.javard.eams.modules.attendance.entity.AttendanceSession;
import com.javard.eams.modules.attendance.entity.AttendanceStatus;
import com.javard.eams.modules.attendance.entity.BreakRecord;
import com.javard.eams.modules.shift.entity.Shift;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.*;

@Slf4j
@Component
public class AttendanceCalculationEngine {

    public void recalculateSession(AttendanceSession session) {
        if (session.getFirstCheckIn() == null) {
            session.setStatus(AttendanceStatus.ABSENT);
            session.setGrossMinutes(0);
            session.setNetWorkMinutes(0);
            return;
        }

        Shift shift = session.getShift();
        ZoneId zone = ZoneId.systemDefault();

        // 1. Evaluate Late Entry
        LocalTime actualInTime = session.getFirstCheckIn().atZone(zone).toLocalTime();
        LocalTime shiftStartTime = shift.getStartTime();
        LocalTime graceDeadline = shiftStartTime.plusMinutes(shift.getGracePeriodMinutes());

        if (actualInTime.isAfter(graceDeadline)) {
            session.setLateEntry(true);
            long lateMinutes = Duration.between(shiftStartTime, actualInTime).toMinutes();
            session.setLateEntryMinutes((int) Math.max(0, lateMinutes));
        } else {
            session.setLateEntry(false);
            session.setLateEntryMinutes(0);
        }

        // 2. Breaks Calculation
        int totalBreakMinutes = 0;
        int unpaidBreakMinutes = 0;
        int paidBreakMinutes = 0;

        if (session.getBreaks() != null) {
            for (BreakRecord b : session.getBreaks()) {
                if (b.getBreakStart() != null && b.getBreakEnd() != null) {
                    int duration = (int) Duration.between(b.getBreakStart(), b.getBreakEnd()).toMinutes();
                    b.setDurationMinutes(duration);
                    totalBreakMinutes += duration;
                    if (b.isPaid()) {
                        paidBreakMinutes += duration;
                    } else {
                        unpaidBreakMinutes += duration;
                    }
                }
            }
        }
        session.setPaidBreakMinutes(paidBreakMinutes);
        session.setUnpaidBreakMinutes(unpaidBreakMinutes);

        // 3. Gross and Net Duration
        if (session.getLastCheckOut() != null) {
            long grossMinutes = Duration.between(session.getFirstCheckIn(), session.getLastCheckOut()).toMinutes();
            session.setGrossMinutes((int) Math.max(0, grossMinutes));

            int netMinutes = session.getGrossMinutes() - unpaidBreakMinutes;
            session.setNetWorkMinutes(Math.max(0, netMinutes));

            // Early exit check
            LocalTime actualOutTime = session.getLastCheckOut().atZone(zone).toLocalTime();
            LocalTime shiftEndTime = shift.getEndTime();
            if (!shift.isOvernight() && actualOutTime.isBefore(shiftEndTime)) {
                long earlyMinutes = Duration.between(actualOutTime, shiftEndTime).toMinutes();
                session.setEarlyExit(true);
                session.setEarlyExitMinutes((int) Math.max(0, earlyMinutes));
            } else {
                session.setEarlyExit(false);
                session.setEarlyExitMinutes(0);
            }

            // 4. Overtime Calculation (if net work > full day threshold)
            int requiredMinutes = shift.getFullDayThresholdMinutes();
            if (session.getNetWorkMinutes() > requiredMinutes) {
                session.setOvertimeMinutes(session.getNetWorkMinutes() - requiredMinutes);
            } else {
                session.setOvertimeMinutes(0);
            }

            // 5. Attendance Status Determination
            if (session.getNetWorkMinutes() >= shift.getFullDayThresholdMinutes()) {
                session.setStatus(AttendanceStatus.PRESENT);
            } else if (session.getNetWorkMinutes() >= shift.getHalfDayThresholdMinutes()) {
                session.setStatus(AttendanceStatus.HALF_DAY);
            } else {
                session.setStatus(AttendanceStatus.ABSENT);
            }
        } else {
            // Still clocked in / ongoing session
            long elapsedGross = Duration.between(session.getFirstCheckIn(), Instant.now()).toMinutes();
            session.setGrossMinutes((int) Math.max(0, elapsedGross));
            session.setNetWorkMinutes((int) Math.max(0, elapsedGross - unpaidBreakMinutes));
        }
    }
}

