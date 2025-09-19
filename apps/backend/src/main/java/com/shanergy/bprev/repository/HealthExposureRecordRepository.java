package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.HealthExposureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface HealthExposureRecordRepository extends JpaRepository<HealthExposureRecord, UUID>, JpaSpecificationExecutor<HealthExposureRecord> {
}

