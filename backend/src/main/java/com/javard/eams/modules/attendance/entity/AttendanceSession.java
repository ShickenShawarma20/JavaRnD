package com.javard.eams.modules.attendance.entity;

import com.javard.eams.common.audit.AuditableEntity;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.shift.entity.Shift;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "attendance_sessions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_employee_work_date", columnNames = {"employee_id", "work_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSession extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "first_check_in")
    private Instant firstCheckIn;

    @Column(name = "last_check_out")
    private Instant lastCheckOut;

    @Column(name = "gross_minutes", nullable = false)
    @Builder.Default
    private int grossMinutes = 0;

    @Column(name = "unpaid_break_minutes", nullable = false)
    @Builder.Default
    private int unpaidBreakMinutes = 0;

    @Column(name = "paid_break_minutes", nullable = false)
    @Builder.Default
    private int paidBreakMinutes = 0;

    @Column(name = "net_work_minutes", nullable = false)
    @Builder.Default
    private int netWorkMinutes = 0;

    @Column(name = "overtime_minutes", nullable = false)
    @Builder.Default
    private int overtimeMinutes = 0;

    @Column(name = "is_late_entry", nullable = false)
    @Builder.Default
    private boolean lateEntry = false;

    @Column(name = "late_entry_minutes", nullable = false)
    @Builder.Default
    private int lateEntryMinutes = 0;

    @Column(name = "is_early_exit", nullable = false)
    @Builder.Default
    private boolean earlyExit = false;

    @Column(name = "early_exit_minutes", nullable = false)
    @Builder.Default
    private int earlyExitMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Column(name = "is_regularized", nullable = false)
    @Builder.Default
    private boolean regularized = false;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AttendanceEvent> events = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BreakRecord> breaks = new ArrayList<>();
}

