package com.workdna.civic.repository;

import com.workdna.civic.domain.model.AuditEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEntryRepository extends JpaRepository<AuditEntryEntity, java.util.UUID> {

    List<AuditEntryEntity> findByIssueIdOrderByResolvedAtAsc(String issueId);
}
