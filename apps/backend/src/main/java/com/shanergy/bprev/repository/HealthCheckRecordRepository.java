package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.HealthCheckRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HealthCheckRecordRepository extends JpaRepository<HealthCheckRecord, UUID>, JpaSpecificationExecutor<HealthCheckRecord> {
    List<HealthCheckRecord> findByFollowUpNeededTrue();
    List<HealthCheckRecord> findByNextCheckDateBefore(LocalDate date);
}

