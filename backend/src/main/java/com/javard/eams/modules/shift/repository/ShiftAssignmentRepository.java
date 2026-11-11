package com.javard.eams.modules.shift.repository;

import com.javard.eams.modules.shift.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.employee.id = :employeeId " +
           "AND sa.effectiveFrom <= :targetDate " +
           "AND (sa.effectiveTo IS NULL OR sa.effectiveTo >= :targetDate) " +
           "ORDER BY sa.effectiveFrom DESC")
    Optional<ShiftAssignment> findActiveAssignmentForDate(
            @Param("employeeId") UUID employeeId,
            @Param("targetDate") LocalDate targetDate);
}

