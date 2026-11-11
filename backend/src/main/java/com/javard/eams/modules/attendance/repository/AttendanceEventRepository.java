package com.javard.eams.modules.attendance.repository;

import com.javard.eams.modules.attendance.entity.AttendanceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, UUID> {
    List<AttendanceEvent> findBySessionIdOrderByEventTimestampAsc(UUID sessionId);
}

