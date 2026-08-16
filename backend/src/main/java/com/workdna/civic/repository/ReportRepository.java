package com.workdna.civic.repository;

import com.workdna.civic.domain.model.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportEntity, java.util.UUID> {

    Optional<ReportEntity> findByReportId(String reportId);

    List<ReportEntity> findByIssueId(String issueId);

    List<ReportEntity> findByIssueIdOrderByTimestampAsc(String issueId);

    List<ReportEntity> findByCategoryAndTimestampBetween(String category, Instant start, Instant end);

    List<ReportEntity> findAllByOrderByTimestampDesc();

    long countByIssueId(String issueId);

    boolean existsByReportId(String reportId);
}

