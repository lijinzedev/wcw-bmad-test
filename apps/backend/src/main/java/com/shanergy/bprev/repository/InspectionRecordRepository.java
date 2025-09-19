package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.InspectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InspectionRecordRepository extends JpaRepository<InspectionRecord, UUID> {
    List<InspectionRecord> findByPlanIdOrderByCreatedAtAsc(UUID planId);
}
