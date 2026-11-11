package com.javard.eams.modules.leave.entity;

import com.javard.eams.modules.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "leave_balances", uniqueConstraints = {
        @UniqueConstraint(name = "uq_employee_leave_balance", columnNames = {"employee_id", "leave_type_id", "year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "total_allocated", nullable = false, precision = 4, scale = 1)
    private BigDecimal totalAllocated;

    @Column(name = "used", nullable = false, precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal used = BigDecimal.ZERO;

    @Column(name = "balance", nullable = false, precision = 4, scale = 1)
    private BigDecimal balance;
}

