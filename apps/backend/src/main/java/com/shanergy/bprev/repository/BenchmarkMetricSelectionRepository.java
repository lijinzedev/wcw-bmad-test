package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.BenchmarkMetricSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BenchmarkMetricSelectionRepository extends JpaRepository<BenchmarkMetricSelection, UUID> {
    List<BenchmarkMetricSelection> findByTemplateIdOrderBySortOrderAsc(UUID templateId);
    void deleteByTemplateId(UUID templateId);
}
