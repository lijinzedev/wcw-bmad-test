package com.shanergy.bprev.repository;

import com.shanergy.bprev.model.KbArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface KbArticleRepository extends JpaRepository<KbArticle, UUID>, JpaSpecificationExecutor<KbArticle> { }

