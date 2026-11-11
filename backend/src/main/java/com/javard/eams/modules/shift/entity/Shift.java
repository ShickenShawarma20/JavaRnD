package com.javard.eams.modules.shift.entity;

import com.javard.eams.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "shift_code", nullable = false, unique = true, length = 20)
    private String shiftCode;

    @Column(name = "shift_name", nullable = false, length = 100)
    private String shiftName; // General (09:00-18:00), Morning, Evening, Night

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "grace_period_minutes", nullable = false)
    @Builder.Default
    private int gracePeriodMinutes = 15;

    @Column(name = "half_day_threshold_minutes", nullable = false)
    @Builder.Default
    private int halfDayThresholdMinutes = 240; // 4 hours

    @Column(name = "full_day_threshold_minutes", nullable = false)
    @Builder.Default
    private int fullDayThresholdMinutes = 480; // 8 hours

    @Column(name = "is_overnight", nullable = false)
    @Builder.Default
    private boolean overnight = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}

