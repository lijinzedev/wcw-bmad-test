package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.InspectionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface InspectionPlanRepository extends JpaRepository<InspectionPlan, UUID>, JpaSpecificationExecutor<InspectionPlan> {

    @org.springframework.data.jpa.repository.Query("""
            SELECT p.mineId, COUNT(p), SUM(CASE WHEN p.status = '已完成' THEN 1 ELSE 0 END)
            FROM InspectionPlan p
            WHERE p.mineId IS NOT NULL
              AND (:level IS NULL OR p.level = :level)
              AND (:start IS NULL OR p.startAt >= :start)
              AND (:end IS NULL OR p.endAt < :end)
            GROUP BY p.mineId
        """)
    java.util.List<Object[]> aggregateCompletionByMine(@org.springframework.data.repository.query.Param("start") java.time.OffsetDateTime start,
                                                       @org.springframework.data.repository.query.Param("end") java.time.OffsetDateTime end,
                                                       @org.springframework.data.repository.query.Param("level") String level);
}
