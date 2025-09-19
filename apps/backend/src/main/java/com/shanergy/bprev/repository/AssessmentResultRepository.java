package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, UUID> {
    List<AssessmentResult> findByCycleIdOrderByScoreDesc(UUID cycleId);
    void deleteByCycleId(UUID cycleId);
    Optional<AssessmentResult> findByCycleIdAndOrganizationId(UUID cycleId, UUID organizationId);
}
