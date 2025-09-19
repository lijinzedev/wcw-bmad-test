package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.AssessmentIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AssessmentIndicatorRepository extends JpaRepository<AssessmentIndicator, UUID> {
    List<AssessmentIndicator> findByCycleIdOrderByWeightDesc(UUID cycleId);
    void deleteByCycleId(UUID cycleId);
    void deleteByCycleIdAndIndicatorIdNotIn(UUID cycleId, Collection<UUID> indicatorIds);
}
