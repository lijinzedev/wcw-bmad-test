package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.HealthCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface HealthCaseRepository extends JpaRepository<HealthCase, UUID>, JpaSpecificationExecutor<HealthCase> {
}

