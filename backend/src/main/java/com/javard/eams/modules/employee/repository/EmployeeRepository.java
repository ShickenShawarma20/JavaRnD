package com.javard.eams.modules.employee.repository;

import com.javard.eams.modules.employee.entity.Employee;
import com.javard.eams.modules.employee.entity.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByUserId(UUID userId);
    boolean existsByEmployeeCode(String employeeCode);

    List<Employee> findByReportingManagerId(UUID reportingManagerId);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:deptId IS NULL OR e.department.id = :deptId) AND " +
           "(:status IS NULL OR e.employmentStatus = :status) AND " +
           "(:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchEmployees(@Param("search") String search,
                                  @Param("deptId") UUID deptId,
                                  @Param("status") EmploymentStatus status,
                                  Pageable pageable);
}

