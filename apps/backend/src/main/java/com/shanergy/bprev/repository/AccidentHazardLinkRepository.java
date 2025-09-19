package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.AccidentHazardLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccidentHazardLinkRepository extends JpaRepository<AccidentHazardLink, UUID> {
    List<AccidentHazardLink> findByAccidentId(UUID accidentId);
    void deleteByAccidentId(UUID accidentId);
    void deleteByAccidentIdAndHazardIdNotIn(UUID accidentId, List<UUID> hazardIds);
}
