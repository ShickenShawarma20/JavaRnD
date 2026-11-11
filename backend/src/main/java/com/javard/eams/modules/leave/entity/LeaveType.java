package com.javard.eams.modules.leave.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "type_code", nullable = false, unique = true, length = 20)
    private String typeCode; // CL, SL, EL, UL, COMP_OFF

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(name = "annual_quota", nullable = false, precision = 4, scale = 1)
    private BigDecimal annualQuota;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private boolean paid = true;

    @Column(name = "carry_forward_limit", nullable = false)
    @Builder.Default
    private int carryForwardLimit = 0;
}

