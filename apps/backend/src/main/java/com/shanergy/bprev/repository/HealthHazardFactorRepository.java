package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.HealthHazardFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface HealthHazardFactorRepository extends JpaRepository<HealthHazardFactor, UUID>, JpaSpecificationExecutor<HealthHazardFactor> {
}

