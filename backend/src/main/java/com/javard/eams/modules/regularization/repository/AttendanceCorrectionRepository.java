package com.javard.eams.modules.regularization.repository;

import com.javard.eams.modules.regularization.entity.AttendanceCorrection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceCorrectionRepository extends JpaRepository<AttendanceCorrection, UUID> {
    List<AttendanceCorrection> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    @Query("SELECT c FROM AttendanceCorrection c WHERE c.employee.reportingManager.id = :managerId " +
           "AND c.status = 'PENDING' ORDER BY c.createdAt DESC")
    List<AttendanceCorrection> findPendingForManager(@Param("managerId") UUID managerId);
}

