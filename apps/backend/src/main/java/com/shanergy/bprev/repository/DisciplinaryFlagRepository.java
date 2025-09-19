package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.DisciplinaryFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DisciplinaryFlagRepository extends JpaRepository<DisciplinaryFlag, UUID>, JpaSpecificationExecutor<DisciplinaryFlag> {
    Optional<DisciplinaryFlag> findFirstByOrganizationIdAndRuleIdAndStatus(UUID organizationId, UUID ruleId, String status);
    List<DisciplinaryFlag> findByOrganizationId(UUID organizationId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT f.organizationId, COUNT(f)
            FROM DisciplinaryFlag f
            WHERE f.status = :status
              AND (:start IS NULL OR f.createdAt >= :start)
              AND (:end IS NULL OR f.createdAt < :end)
            GROUP BY f.organizationId
        """)
    java.util.List<Object[]> aggregateFlagsByOrg(@org.springframework.data.repository.query.Param("status") String status,
                                                @org.springframework.data.repository.query.Param("start") java.time.OffsetDateTime start,
                                                @org.springframework.data.repository.query.Param("end") java.time.OffsetDateTime end);
}
