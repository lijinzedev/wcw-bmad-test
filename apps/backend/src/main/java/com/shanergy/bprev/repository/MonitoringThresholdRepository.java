package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.MonitoringThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MonitoringThresholdRepository extends JpaRepository<MonitoringThreshold, UUID>, JpaSpecificationExecutor<MonitoringThreshold> {
    Optional<MonitoringThreshold> findFirstByMetricCodeAndLocationPattern(String metricCode, String locationPattern);
}

