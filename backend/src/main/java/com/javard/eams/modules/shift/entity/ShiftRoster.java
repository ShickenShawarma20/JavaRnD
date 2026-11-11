package com.javard.eams.modules.shift.entity;

import com.javard.eams.common.audit.AuditableEntity;
import com.javard.eams.modules.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "shift_rosters", uniqueConstraints = {
        @UniqueConstraint(name = "uq_emp_roster_date", columnNames = {"employee_id", "roster_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftRoster extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "roster_date", nullable = false)
    private LocalDate rosterDate;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    private boolean published = true;
}

