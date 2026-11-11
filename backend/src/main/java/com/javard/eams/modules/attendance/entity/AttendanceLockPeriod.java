package com.javard.eams.modules.attendance.entity;

import com.javard.eams.common.audit.AuditableEntity;
import com.javard.eams.modules.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance_lock_periods", uniqueConstraints = {
        @UniqueConstraint(name = "uq_lock_year_month", columnNames = {"year", "month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceLockPeriod extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private boolean locked = true;

    @Column(name = "locked_at", nullable = false)
    private Instant lockedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by", nullable = false)
    private Employee lockedBy;

    @Column(name = "unlock_reason", columnDefinition = "TEXT")
    private String unlockReason;
}

