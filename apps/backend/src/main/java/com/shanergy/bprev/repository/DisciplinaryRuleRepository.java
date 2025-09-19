package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.DisciplinaryRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DisciplinaryRuleRepository extends JpaRepository<DisciplinaryRule, UUID>, JpaSpecificationExecutor<DisciplinaryRule> {
}
