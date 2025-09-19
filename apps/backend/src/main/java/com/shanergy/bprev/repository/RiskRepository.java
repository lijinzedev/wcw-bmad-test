package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.Risk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.UUID;

public interface RiskRepository extends JpaRepository<Risk, UUID>, JpaSpecificationExecutor<Risk> {
    long countByResponsibleOrgIdIn(Collection<UUID> organizationIds);
}
