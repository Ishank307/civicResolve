package com.workdna.civic.repository;

import com.workdna.civic.domain.model.ResolutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResolutionRepository extends JpaRepository<ResolutionEntity, java.util.UUID> {

    Optional<ResolutionEntity> findTopByIssueIdOrderByVersionDesc(String issueId);

    List<ResolutionEntity> findByIssueIdOrderByVersionAsc(String issueId);

    @Query("SELECT DISTINCT r.issueId FROM ResolutionEntity r")
    List<String> findDistinctIssueIds();
}

