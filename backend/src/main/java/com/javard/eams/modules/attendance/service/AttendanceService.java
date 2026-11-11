package com.javard.eams.modules.attendance.service;

import com.javard.eams.common.exception.BadRequestException;
import com.javard.eams.common.exception.ResourceNotFoundException;
import com.javard.eams.common.response.PageResponse;
import com.javard.eams.modules.attendance.dto.req.PunchRequest;
import com.javard.eams.modules.attendance.dto.res.AttendanceRecordResponse;
import com.javard.eams.modules.attendance.engine.AttendanceCalculationEngine;
import com.javard.eams.modules.attendance.entity.*;
import com.javard.eams.modules.attendance.repository.AttendanceEventRepository;
import com.javard.eams.modules.attendance.repository.AttendanceSessionRepository;
import com.javard.eams.modules.attendance.repository.BreakRecordRepository;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import com.javard.eams.modules.shift.entity.Shift;
import com.javard.eams.modules.shift.entity.ShiftAssignment;
import com.javard.eams.modules.shift.repository.ShiftAssignmentRepository;
import com.javard.eams.modules.shift.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceEventRepository eventRepository;
    private final BreakRecordRepository breakRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final AttendanceCalculationEngine calculationEngine;

    @Transactional
    public AttendanceRecordResponse processPunch(UUID employeeId, PunchRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        LocalDate today = LocalDate.now();
        Instant now = Instant.now();

        // 1. Resolve or create today's session
        AttendanceSession session = sessionRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseGet(() -> {
                    Shift shift = resolveActiveShift(employeeId, today);
                    return AttendanceSession.builder()
                            .employee(employee)
                            .shift(shift)
                            .workDate(today)
                            .status(AttendanceStatus.PRESENT)
                            .build();
                });

        // 2. Process Punch by Type
        switch (request.getEventType()) {
            case CHECK_IN:
                if (session.getFirstCheckIn() != null && session.getLastCheckOut() == null) {
                    throw new BadRequestException("You are already checked in!");
                }
                if (session.getFirstCheckIn() == null) {
                    session.setFirstCheckIn(now);
                }
                break;

            case CHECK_OUT:
                if (session.getFirstCheckIn() == null) {
                    throw new BadRequestException("Cannot check out without checking in first");
                }
                // Close any open break automatically if employee checks out
                breakRepository.findFirstBySessionIdAndBreakEndIsNullOrderByBreakStartDesc(session.getId())
                        .ifPresent(openBreak -> {
                            openBreak.setBreakEnd(now);
                            breakRepository.save(openBreak);
                        });
                session.setLastCheckOut(now);
                break;

            case BREAK_START:
                if (session.getFirstCheckIn() == null || session.getLastCheckOut() != null) {
                    throw new BadRequestException("Cannot start a break while not clocked in");
                }
                Optional<BreakRecord> currentBreak = breakRepository
                        .findFirstBySessionIdAndBreakEndIsNullOrderByBreakStartDesc(session.getId());
                if (currentBreak.isPresent()) {
                    throw new BadRequestException("You are already on an active break");
                }
                BreakRecord newBreak = BreakRecord.builder()
                        .session(session)
                        .breakStart(now)
                        .paid(false)
                        .build();
                session.getBreaks().add(newBreak);
                break;

            case BREAK_END:
                BreakRecord activeBreak = breakRepository
                        .findFirstBySessionIdAndBreakEndIsNullOrderByBreakStartDesc(session.getId())
                        .orElseThrow(() -> new BadRequestException("No active break found to end"));
                activeBreak.setBreakEnd(now);
                break;
        }

        // 3. Save event audit
        AttendanceEvent event = AttendanceEvent.builder()
                .session(session)
                .eventType(request.getEventType())
                .eventTimestamp(now)
                .ipAddress(request.getIpAddress())
                .deviceInfo(request.getDeviceInfo())
                .build();
        session.getEvents().add(event);

        // 4. Run Calculation Engine
        calculationEngine.recalculateSession(session);
        AttendanceSession saved = sessionRepository.save(session);

        return AttendanceRecordResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AttendanceRecordResponse getTodaySession(UUID employeeId) {
        LocalDate today = LocalDate.now();
        return sessionRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .map(AttendanceRecordResponse::from)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getMonthlyHistory(UUID employeeId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        return sessionRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employeeId, start, end)
                .stream()
                .map(AttendanceRecordResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceRecordResponse> getDailySheet(
            LocalDate date, UUID departmentId, AttendanceStatus status, Pageable pageable) {
        Page<AttendanceSession> page = sessionRepository.findByWorkDateAndDepartmentAndStatus(date, departmentId, status, pageable);
        return PageResponse.from(page.map(AttendanceRecordResponse::from));
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getTeamAttendance(UUID managerId, LocalDate date) {
        return sessionRepository.findByWorkDateAndManager(date, managerId)
                .stream()
                .map(AttendanceRecordResponse::from)
                .collect(Collectors.toList());
    }

    private Shift resolveActiveShift(UUID employeeId, LocalDate date) {
        return shiftAssignmentRepository.findActiveAssignmentForDate(employeeId, date)
                .map(ShiftAssignment::getShift)
                .orElseGet(() -> shiftRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No default shift configured in the system")));
    }
}

