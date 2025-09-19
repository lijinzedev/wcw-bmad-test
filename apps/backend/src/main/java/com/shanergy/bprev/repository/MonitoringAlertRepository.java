package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.MonitoringAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface MonitoringAlertRepository extends JpaRepository<MonitoringAlert, UUID>, JpaSpecificationExecutor<MonitoringAlert> {
    Optional<MonitoringAlert> findByExternalEventId(String externalEventId);
    long countByOccurredAtAfter(OffsetDateTime timestamp);
    Optional<MonitoringAlert> findTopByOrderByOccurredAtDesc();
}
