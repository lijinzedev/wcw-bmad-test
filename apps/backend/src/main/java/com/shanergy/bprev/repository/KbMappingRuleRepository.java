package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.KbMappingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface KbMappingRuleRepository extends JpaRepository<KbMappingRule, UUID>, JpaSpecificationExecutor<KbMappingRule> { }

