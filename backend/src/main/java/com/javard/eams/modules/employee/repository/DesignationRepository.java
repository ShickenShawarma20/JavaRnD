package com.javard.eams.modules.employee.repository;

import com.javard.eams.modules.employee.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, UUID> {
    Optional<Designation> findByDesignationCode(String designationCode);
    boolean existsByDesignationCode(String designationCode);
}

