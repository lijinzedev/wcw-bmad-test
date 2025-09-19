package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.BenchmarkExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BenchmarkExecutionRepository extends JpaRepository<BenchmarkExecution, UUID> {
    Optional<BenchmarkExecution> findTopByTemplateIdAndParametersHashOrderByExecutedAtDesc(UUID templateId, String parametersHash);
    Optional<BenchmarkExecution> findTopByTemplateIdOrderByExecutedAtDesc(UUID templateId);
}
