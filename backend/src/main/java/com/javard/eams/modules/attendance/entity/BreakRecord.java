package com.javard.eams.modules.attendance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "break_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;

    @Column(name = "break_start", nullable = false)
    private Instant breakStart;

    @Column(name = "break_end")
    private Instant breakEnd;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private boolean paid = false;
}

