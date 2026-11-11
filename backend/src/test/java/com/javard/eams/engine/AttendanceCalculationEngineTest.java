package com.javard.eams.engine;

import com.javard.eams.modules.attendance.engine.AttendanceCalculationEngine;
import com.javard.eams.modules.attendance.entity.AttendanceSession;
import com.javard.eams.modules.attendance.entity.AttendanceStatus;
import com.javard.eams.modules.attendance.entity.BreakRecord;
import com.javard.eams.modules.shift.entity.Shift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceCalculationEngineTest {

    private AttendanceCalculationEngine engine;
    private Shift generalShift;

    @BeforeEach
    void setUp() {
        engine = new AttendanceCalculationEngine();
        generalShift = Shift.builder()
                .shiftCode("GEN")
                .shiftName("General 09:00 - 18:00")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .gracePeriodMinutes(15)
                .halfDayThresholdMinutes(240) // 4 hrs
                .fullDayThresholdMinutes(480) // 8 hrs
                .overnight(false)
                .build();
    }

    @Test
    void testOntimeCheckinAndFullDayPresentation() {
        LocalDate today = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();

        Instant checkIn = today.atTime(9, 10).atZone(zone).toInstant(); // 09:10
        Instant checkOut = today.atTime(18, 0).atZone(zone).toInstant(); // 18:00 -> 530 minutes

        AttendanceSession session = AttendanceSession.builder()
                .shift(generalShift)
                .workDate(today)
                .firstCheckIn(checkIn)
                .lastCheckOut(checkOut)
                .breaks(new ArrayList<>())
                .build();

        engine.recalculateSession(session);

        assertFalse(session.isLateEntry());
        assertEquals(0, session.getLateEntryMinutes());
        assertEquals(530, session.getGrossMinutes()); // from 09:10 to 18:00 is exactly 530 minutes
        assertEquals(530, session.getNetWorkMinutes());
        assertEquals(50, session.getOvertimeMinutes()); // 530 - 480 required = 50 mins OT
        assertEquals(AttendanceStatus.PRESENT, session.getStatus());
    }

    @Test
    void testLateEntryAndUnpaidBreakDeduction() {
        LocalDate today = LocalDate.now();
        ZoneId zone = ZoneId.systemDefault();

        Instant checkIn = today.atTime(9, 45).atZone(zone).toInstant(); // 45 min late
        Instant checkOut = today.atTime(17, 0).atZone(zone).toInstant(); // 09:45 to 17:00 = 7h 15m = 435 minutes gross

        // 60 minutes unpaid lunch break
        BreakRecord lunchBreak = BreakRecord.builder()
                .breakStart(today.atTime(13, 0).atZone(zone).toInstant())
                .breakEnd(today.atTime(14, 0).atZone(zone).toInstant())
                .paid(false)
                .build();

        AttendanceSession session = AttendanceSession.builder()
                .shift(generalShift)
                .workDate(today)
                .firstCheckIn(checkIn)
                .lastCheckOut(checkOut)
                .breaks(new ArrayList<>(List.of(lunchBreak)))
                .build();

        engine.recalculateSession(session);

        assertTrue(session.isLateEntry());
        assertEquals(45, session.getLateEntryMinutes());
        assertTrue(session.isEarlyExit());
        assertEquals(60, session.getEarlyExitMinutes());
        assertEquals(60, session.getUnpaidBreakMinutes());
        assertEquals(435, session.getGrossMinutes()); // 435 gross
        assertEquals(375, session.getNetWorkMinutes()); // 435 - 60 = 375 net
        assertEquals(AttendanceStatus.HALF_DAY, session.getStatus()); // > 240 mins but < 480 mins
    }
}
