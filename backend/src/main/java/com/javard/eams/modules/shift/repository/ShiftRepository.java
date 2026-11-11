package com.javard.eams.modules.shift.repository;

import com.javard.eams.modules.shift.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    Optional<Shift> findByShiftCode(String shiftCode);
    boolean existsByShiftCode(String shiftCode);
}

