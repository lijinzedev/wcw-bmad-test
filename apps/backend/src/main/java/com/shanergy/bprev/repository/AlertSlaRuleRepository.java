package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.AlertSlaRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AlertSlaRuleRepository extends JpaRepository<AlertSlaRule, UUID>, JpaSpecificationExecutor<AlertSlaRule> {
}

