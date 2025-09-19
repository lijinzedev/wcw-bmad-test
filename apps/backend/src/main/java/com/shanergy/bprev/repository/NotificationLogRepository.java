package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID>, JpaSpecificationExecutor<NotificationLog> {
}

