package com.javard.eams.modules.leave.repository;

import com.javard.eams.modules.leave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {
    Optional<LeaveType> findByTypeCode(String typeCode);
}

