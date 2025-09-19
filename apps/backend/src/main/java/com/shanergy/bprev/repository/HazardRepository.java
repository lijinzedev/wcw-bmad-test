package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.Hazard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HazardRepository extends JpaRepository<Hazard, UUID>, JpaSpecificationExecutor<Hazard> {
    Page<Hazard> findByReporterIdOrderByReportedAtDesc(UUID reporterId, Pageable pageable);

    @Query(value = """
            SELECT r.responsible_org_id AS org_id, COUNT(*) AS total
            FROM hazards h
            JOIN risks r ON h.risk_id = r.risk_id
            WHERE (:since IS NULL OR h.reported_at >= :since)
              AND (:levelFilter IS NULL OR h.level = :levelFilter)
              AND (:statusFilter = FALSE OR h.status NOT IN ('已关闭', '已作废'))
            GROUP BY r.responsible_org_id
        """, nativeQuery = true)
    List<Object[]> aggregateHazardCountByOrg(@Param("since") java.time.OffsetDateTime since,
                                             @Param("levelFilter") String levelFilter,
                                             @Param("statusFilter") boolean statusFilter);

    @Query(value = """
            SELECT r.responsible_org_id AS org_id, COUNT(*) AS total
            FROM hazards h
            JOIN risks r ON h.risk_id = r.risk_id
            WHERE (:start IS NULL OR h.reported_at >= :start)
              AND (:end IS NULL OR h.reported_at < :end)
              AND (:levelFilter IS NULL OR h.level = :levelFilter)
              AND (:onlyOpen = FALSE OR h.status NOT IN ('已关闭', '已作废'))
            GROUP BY r.responsible_org_id
        """, nativeQuery = true)
    List<Object[]> aggregateHazardCountByOrgBetween(@Param("start") java.time.OffsetDateTime start,
                                                    @Param("end") java.time.OffsetDateTime end,
                                                    @Param("levelFilter") String levelFilter,
                                                    @Param("onlyOpen") boolean onlyOpen);

    @Query(value = """
            SELECT r.responsible_org_id AS org_id, COUNT(*) AS total
            FROM hazards h
            JOIN risks r ON h.risk_id = r.risk_id
            WHERE h.rectification_deadline IS NOT NULL
              AND h.rectification_deadline < :deadline
              AND h.status NOT IN ('已关闭', '已作废')
              AND (:start IS NULL OR h.reported_at >= :start)
              AND (:end IS NULL OR h.reported_at < :end)
            GROUP BY r.responsible_org_id
        """, nativeQuery = true)
    List<Object[]> aggregateOverdueHazardsByOrg(@Param("deadline") java.time.LocalDate deadline,
                                                @Param("start") java.time.OffsetDateTime start,
                                                @Param("end") java.time.OffsetDateTime end);
}
