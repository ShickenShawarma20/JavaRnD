package com.javard.eams.modules.attendance.repository;

import com.javard.eams.modules.attendance.entity.BreakRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BreakRecordRepository extends JpaRepository<BreakRecord, UUID> {
    Optional<BreakRecord> findFirstBySessionIdAndBreakEndIsNullOrderByBreakStartDesc(UUID sessionId);
}

