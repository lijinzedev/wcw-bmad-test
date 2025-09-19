package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.AssessmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AssessmentDetailRepository extends JpaRepository<AssessmentDetail, UUID> {
    List<AssessmentDetail> findByResultId(UUID resultId);
    void deleteByResultId(UUID resultId);
    void deleteByResultIdIn(Collection<UUID> resultIds);
}
