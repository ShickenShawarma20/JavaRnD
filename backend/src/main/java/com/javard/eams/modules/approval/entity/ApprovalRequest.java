package com.javard.eams.modules.approval.entity;

import com.javard.eams.common.audit.AuditableEntity;
import com.javard.eams.modules.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "approval_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // LEAVE, REGULARIZATION, SHIFT_SWAP, OVERTIME

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Employee requester;

    @Column(name = "current_level", nullable = false)
    @Builder.Default
    private int currentLevel = 1;

    @Column(name = "total_levels", nullable = false)
    @Builder.Default
    private int totalLevels = 1;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, CANCELLED

    @Column(name = "summary", nullable = false, length = 255)
    private String summary;

    @OneToMany(mappedBy = "approvalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApprovalActionLog> actionLogs = new ArrayList<>();
}

