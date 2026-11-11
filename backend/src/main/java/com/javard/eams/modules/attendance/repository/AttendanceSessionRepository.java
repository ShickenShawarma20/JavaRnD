package com.javard.eams.modules.attendance.repository;

import com.javard.eams.modules.attendance.entity.AttendanceSession;
import com.javard.eams.modules.attendance.entity.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {

    Optional<AttendanceSession> findByEmployeeIdAndWorkDate(UUID employeeId, LocalDate workDate);

    List<AttendanceSession> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
            UUID employeeId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM AttendanceSession s WHERE s.workDate = :workDate " +
           "AND (:deptId IS NULL OR s.employee.department.id = :deptId) " +
           "AND (:status IS NULL OR s.status = :status)")
    Page<AttendanceSession> findByWorkDateAndDepartmentAndStatus(
            @Param("workDate") LocalDate workDate,
            @Param("deptId") UUID deptId,
            @Param("status") AttendanceStatus status,
            Pageable pageable);

    @Query("SELECT s FROM AttendanceSession s WHERE s.workDate = :workDate " +
           "AND s.employee.reportingManager.id = :managerId")
    List<AttendanceSession> findByWorkDateAndManager(
            @Param("workDate") LocalDate workDate,
            @Param("managerId") UUID managerId);

    List<AttendanceSession> findByStatusAndWorkDate(AttendanceStatus status, LocalDate workDate);
}

