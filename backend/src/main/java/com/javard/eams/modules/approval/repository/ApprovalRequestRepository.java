package com.javard.eams.modules.approval.repository;

import com.javard.eams.modules.approval.entity.ApprovalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {

    @Query("SELECT ar FROM ApprovalRequest ar WHERE ar.requester.reportingManager.id = :managerId " +
           "AND ar.status = 'PENDING' ORDER BY ar.createdAt DESC")
    List<ApprovalRequest> findPendingForManager(@Param("managerId") UUID managerId);

    Page<ApprovalRequest> findByRequesterIdOrderByCreatedAtDesc(UUID requesterId, Pageable pageable);

    List<ApprovalRequest> findByStatus(String status);
}

