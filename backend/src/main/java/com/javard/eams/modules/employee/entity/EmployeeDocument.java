package com.javard.eams.modules.employee.entity;

import com.javard.eams.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDocument extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; // ID_PROOF, PASSPORT, DEGREE, CONTRACT, RESUME

    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;
}

