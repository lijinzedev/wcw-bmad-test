package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.Accident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AccidentRepository extends JpaRepository<Accident, UUID>, JpaSpecificationExecutor<Accident> {
    @Query(value = """
            SELECT organization_id AS org_id,
                   COUNT(*) AS total,
                   COALESCE(SUM(COALESCE(fatality_count, 0)), 0) AS fatalities
            FROM accidents
            WHERE (:start IS NULL OR occurred_at >= :start)
              AND (:end IS NULL OR occurred_at < :end)
            GROUP BY organization_id
        """, nativeQuery = true)
    java.util.List<Object[]> aggregateByOrganization(@Param("start") java.time.OffsetDateTime start,
                                                     @Param("end") java.time.OffsetDateTime end);
}
