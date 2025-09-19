package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.BenchmarkTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface BenchmarkTemplateRepository extends JpaRepository<BenchmarkTemplate, UUID>, JpaSpecificationExecutor<BenchmarkTemplate> {
}
