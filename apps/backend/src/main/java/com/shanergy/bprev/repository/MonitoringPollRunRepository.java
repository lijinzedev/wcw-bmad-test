package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.MonitoringPollRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MonitoringPollRunRepository extends JpaRepository<MonitoringPollRun, UUID> {
}

