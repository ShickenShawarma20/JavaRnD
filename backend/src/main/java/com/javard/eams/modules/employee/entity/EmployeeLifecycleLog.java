package com.javard.eams.modules.employee.entity;

import com.javard.eams.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_lifecycle_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLifecycleLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // ONBOARDED, PROBATION_EXTENDED, CONFIRMED, PROMOTED, TRANSFERRED, RESIGNED, TERMINATED

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "previous_value", length = 100)
    private String previousValue;

    @Column(name = "new_value", length = 100)
    private String newValue;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}

