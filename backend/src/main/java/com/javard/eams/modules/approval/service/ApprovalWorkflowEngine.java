package com.javard.eams.modules.approval.service;

import com.javard.eams.common.exception.BadRequestException;
import com.javard.eams.common.exception.ResourceNotFoundException;
import com.javard.eams.modules.approval.entity.ApprovalActionLog;
import com.javard.eams.modules.approval.entity.ApprovalRequest;
import com.javard.eams.modules.approval.repository.ApprovalRequestRepository;
import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.repository.EmployeeRepository;
import com.javard.eams.modules.leave.service.LeaveService;
import com.javard.eams.modules.regularization.service.RegularizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalWorkflowEngine {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveService leaveService;
    private final RegularizationService regularizationService;

    @Transactional
    public ApprovalRequest submitForApproval(String entityType, UUID entityId, UUID requesterId, String summary) {
        Employee requester = employeeRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));

        ApprovalRequest request = ApprovalRequest.builder()
                .entityType(entityType)
                .entityId(entityId)
                .requester(requester)
                .currentLevel(1)
                .totalLevels(1) // Single-level by default, scalable to multi-tier
                .status("PENDING")
                .summary(summary)
                .build();

        return approvalRequestRepository.save(request);
    }

    @Transactional
    public ApprovalRequest processApprovalAction(UUID approvalRequestId, UUID reviewerId, boolean approve, String remarks) {
        ApprovalRequest request = approvalRequestRepository.findById(approvalRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found"));

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new BadRequestException("Request is already finalized with status: " + request.getStatus());
        }

        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        ApprovalActionLog actionLog = ApprovalActionLog.builder()
                .approvalRequest(request)
                .levelNumber(request.getCurrentLevel())
                .reviewer(reviewer)
                .action(approve ? "APPROVED" : "REJECTED")
                .remarks(remarks)
                .actionTimestamp(Instant.now())
                .build();
        request.getActionLogs().add(actionLog);

        if (approve) {
            request.setStatus("APPROVED");

            // Dispatch to target domain mutator
            switch (request.getEntityType().toUpperCase()) {
                case "LEAVE":
                    leaveService.actionLeave(request.getEntityId(), reviewerId, true, remarks);
                    break;
                case "REGULARIZATION":
                    regularizationService.actionCorrection(request.getEntityId(), reviewerId, true, remarks);
                    break;
                default:
                    log.info("Generic approval completed for type: {}", request.getEntityType());
            }
        } else {
            request.setStatus("REJECTED");
            switch (request.getEntityType().toUpperCase()) {
                case "LEAVE":
                    leaveService.actionLeave(request.getEntityId(), reviewerId, false, remarks);
                    break;
                case "REGULARIZATION":
                    regularizationService.actionCorrection(request.getEntityId(), reviewerId, false, remarks);
                    break;
            }
        }

        return approvalRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequest> getUnifiedInboxForManager(UUID managerId) {
        return approvalRequestRepository.findPendingForManager(managerId);
    }
}

