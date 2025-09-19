package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.SlaBreach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface SlaBreachRepository extends JpaRepository<SlaBreach, UUID>, JpaSpecificationExecutor<SlaBreach> {
}

