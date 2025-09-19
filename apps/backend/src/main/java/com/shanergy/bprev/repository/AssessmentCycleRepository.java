package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.AssessmentCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AssessmentCycleRepository extends JpaRepository<AssessmentCycle, UUID>, JpaSpecificationExecutor<AssessmentCycle> {
}
