package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.Behavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BehaviorRepository extends JpaRepository<Behavior, UUID>, JpaSpecificationExecutor<Behavior> {

    @Query("select b.behaviorType, count(b) from Behavior b group by b.behaviorType")
    List<Object[]> countByBehaviorType();

    @Query("select b.personName, count(b) from Behavior b where b.personName is not null and b.personName <> '' group by b.personName order by count(b) desc")
    List<Object[]> countByPersonName();

    @Query(value = """
            SELECT u.organization_id, COUNT(*)
            FROM behaviors b
            JOIN users u ON b.handler_id = u.user_id
            WHERE u.organization_id IS NOT NULL
              AND (:start IS NULL OR b.occurred_at >= :start)
              AND (:end IS NULL OR b.occurred_at < :end)
            GROUP BY u.organization_id
        """, nativeQuery = true)
    List<Object[]> aggregateBehaviorCountByOrg(@org.springframework.data.repository.query.Param("start") java.time.OffsetDateTime start,
                                               @org.springframework.data.repository.query.Param("end") java.time.OffsetDateTime end);
}
