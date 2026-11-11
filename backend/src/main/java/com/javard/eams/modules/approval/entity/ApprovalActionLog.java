package com.javard.eams.modules.approval.entity;

import com.javard.eams.modules.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approval_action_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id", nullable = false)
    private ApprovalRequest approvalRequest;

    @Column(name = "level_number", nullable = false)
    private int levelNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;

    @Column(name = "action", nullable = false, length = 30)
    private String action; // APPROVED, REJECTED, DELEGATED

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "action_timestamp", nullable = false)
    @Builder.Default
    private Instant actionTimestamp = Instant.now();
}

