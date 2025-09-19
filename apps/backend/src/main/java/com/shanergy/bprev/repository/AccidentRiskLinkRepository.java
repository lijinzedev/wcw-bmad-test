package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.AccidentRiskLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccidentRiskLinkRepository extends JpaRepository<AccidentRiskLink, UUID> {
    List<AccidentRiskLink> findByAccidentId(UUID accidentId);
    void deleteByAccidentId(UUID accidentId);
    void deleteByAccidentIdAndRiskIdNotIn(UUID accidentId, List<UUID> riskIds);
}
