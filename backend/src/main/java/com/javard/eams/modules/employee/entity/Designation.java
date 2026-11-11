package com.javard.eams.modules.employee.entity;

import com.javard.eams.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "designations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Designation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "designation_code", nullable = false, unique = true, length = 20)
    private String designationCode;

    @Column(name = "designation_name", nullable = false, length = 100)
    private String designationName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}

