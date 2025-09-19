package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.HazardUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HazardUpdateRepository extends JpaRepository<HazardUpdate, UUID> {
    List<HazardUpdate> findByHazardIdOrderByTimestampAsc(UUID hazardId);
    Optional<HazardUpdate> findFirstByHazardIdOrderByTimestampDesc(UUID hazardId);
}
