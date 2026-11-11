package com.javard.eams.modules.leave.repository;

import com.javard.eams.modules.leave.entity.LeaveRequest;
import com.javard.eams.modules.leave.entity.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    Page<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.reportingManager.id = :managerId " +
           "AND (:status IS NULL OR lr.status = :status) ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findByManagerAndStatus(@Param("managerId") UUID managerId, @Param("status") LeaveStatus status);

    List<LeaveRequest> findByStatus(LeaveStatus status);
}

