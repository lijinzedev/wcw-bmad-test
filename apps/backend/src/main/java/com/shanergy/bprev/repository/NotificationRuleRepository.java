package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.NotificationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface NotificationRuleRepository extends JpaRepository<NotificationRule, UUID>, JpaSpecificationExecutor<NotificationRule> {
}

