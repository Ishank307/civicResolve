package com.workdna.civic.service.resolution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workdna.civic.api.dto.IssueSummary;
import com.workdna.civic.api.dto.ReportRequest;
import com.workdna.civic.api.dto.ReportResponse;
import com.workdna.civic.api.dto.ResolutionSummary;
import com.workdna.civic.domain.enums.ActionTaken;
import com.workdna.civic.domain.enums.ReportSource;
import com.workdna.civic.domain.model.ReportEntity;
import com.workdna.civic.domain.model.ResolutionEntity;
import com.workdna.civic.repository.ReportRepository;
import com.workdna.civic.repository.ResolutionRepository;
import com.workdna.civic.service.audit.AuditService;
import com.workdna.civic.service.identity.IdentityResolutionService;
import com.workdna.civic.service.temporal.TemporalConflictService;
import com.workdna.civic.util.IssueIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ResolutionEngineService {

    private final ReportRepository reportRepository;
    private final ResolutionRepository resolutionRepository;
    private final IdentityResolutionService identityResolutionService;
    private final TemporalConflictService temporalConflictService;
    private final AuditService auditService;
    private final IssueIdGenerator issueIdGenerator;
    private final ObjectMapper objectMapper;

    public ResolutionEngineService(
            ReportRepository reportRepository,
            ResolutionRepository resolutionRepository,
            IdentityResolutionService identityResolutionService,
            TemporalConflictService temporalConflictService,
            AuditService auditService,
            IssueIdGenerator issueIdGenerator,
            ObjectMapper objectMapper
    ) {
        this.reportRepository = reportRepository;
        this.resolutionRepository = resolutionRepository;
        this.identityResolutionService = identityResolutionService;
        this.temporalConflictService = temporalConflictService;
        this.auditService = auditService;
        this.issueIdGenerator = issueIdGenerator;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ReportResponse processReport(ReportRequest request) {
        // 1. Idempotency check: same reportId returns identical prior response
        if (reportRepository.existsByReportId(request.reportId())) {
            ReportEntity existing = reportRepository.findByReportId(request.reportId()).orElseThrow();
            return toIdempotentResponse(existing);
        }

        // 2. Identity resolution & Issue ID generation
        String identityId = identityResolutionService.resolveIdentity(request);
        String issueId = issueIdGenerator.generate(request);

        // 3. Persist incoming report
        ReportEntity newReport = mapToEntity(request, identityId, issueId);
        reportRepository.save(newReport);

        // 4. Fetch existing resolutions and all chronologically ordered reports for this issue
        Optional<ResolutionEntity> latestResolutionOpt = resolutionRepository.findTopByIssueIdOrderByVersionDesc(issueId);
        List<ReportEntity> allReports = reportRepository.findByIssueIdOrderByTimestampAsc(issueId);

        // 5. Evaluate state, action and evidence using tie-breaking and temporal replay logic
        ResolutionEvaluation eval = evaluateResolution(issueId, newReport, allReports, latestResolutionOpt.orElse(null));

        // 6. Persist versioned resolution
        ResolutionEntity resolution = persistResolution(
                eval.effectiveReport(),
                issueId,
                identityId,
                eval.actionTaken(),
                eval.evidence(),
                latestResolutionOpt
        );

        // 7. Record decision audit entry with stateBefore and stateAfter
        auditService.recordDecision(
                issueId,
                eval.actionTaken().name(),
                eval.evidence(),
                identityId,
                latestResolutionOpt.orElse(null),
                resolution
        );

        // 8. If conflict detected and resolution pending, throw HTTP 409
        if (eval.actionTaken() == ActionTaken.CONFLICT) {
            throw new ConflictResolutionPendingException(
                    issueId,
                    eval.evidence(),
                    "Conflicting reports detected for issue " + issueId + " with evidence " + eval.evidence()
            );
        }

        return new ReportResponse(
                request.reportId(),
                identityId,
                issueId,
                eval.actionTaken(),
                identityId,
                resolution.getLastModified(),
                eval.evidence()
        );
    }

    private ResolutionEvaluation evaluateResolution(
            String issueId,
            ReportEntity newReport,
            List<ReportEntity> allReports,
            ResolutionEntity previousResolution
    ) {
        List<String> evidence = allReports.stream().map(ReportEntity::getReportId).toList();

        // If explicitly marked as duplicate
        if (newReport.isDuplicate()) {
            ReportEntity best = selectWinningReport(allReports);
            return new ResolutionEvaluation(ActionTaken.DUPLICATE, best, evidence);
        }

        // First report for this issue window
        if (previousResolution == null || allReports.size() <= 1) {
            return new ResolutionEvaluation(ActionTaken.NEW_ISSUE, newReport, evidence);
        }

        // Multiple reports: apply tie-breaking and detect potential contradictions
        ReportEntity winningReport = selectWinningReport(allReports);

        // Check if there is an irreconcilable conflict (contradicting resolved statuses with identical rank & timestamp)
        boolean hasTieConflict = checkForConflict(allReports);
        if (hasTieConflict) {
            return new ResolutionEvaluation(ActionTaken.CONFLICT, winningReport, evidence);
        }

        return new ResolutionEvaluation(ActionTaken.REFINED, winningReport, evidence);
    }

    /**
     * Tie-breaking precedence rule:
     * 1. Source: MOBILE (2) > WEB (1)
     * 2. Status: isResolved == true (2) > false (1)
     * 3. Timestamp: Latest timestamp wins
     */
    private ReportEntity selectWinningReport(List<ReportEntity> reports) {
        return reports.stream()
                .filter(r -> !r.isDuplicate())
                .max(this::compareReportPriority)
                .orElse(reports.get(reports.size() - 1));
    }

    private int compareReportPriority(ReportEntity a, ReportEntity b) {
        // 1. Prefer mobile source
        int sourceA = a.getSource() == ReportSource.MOBILE ? 2 : 1;
        int sourceB = b.getSource() == ReportSource.MOBILE ? 2 : 1;
        if (sourceA != sourceB) {
            return Integer.compare(sourceA, sourceB);
        }

        // 2. Prefer resolved: true
        int resolvedA = a.isResolved() ? 2 : 1;
        int resolvedB = b.isResolved() ? 2 : 1;
        if (resolvedA != resolvedB) {
            return Integer.compare(resolvedA, resolvedB);
        }

        // 3. Prefer latest timestamp
        return a.getTimestamp().compareTo(b.getTimestamp());
    }

    private boolean checkForConflict(List<ReportEntity> reports) {
        if (reports.size() < 2) {
            return false;
        }

        // Compare top two reports excluding duplicates
        List<ReportEntity> nonDuplicates = reports.stream()
                .filter(r -> !r.isDuplicate())
                .sorted(Comparator.comparingInt(this::calculatePriorityScore).reversed())
                .toList();

        if (nonDuplicates.size() < 2) {
            return false;
        }

        ReportEntity first = nonDuplicates.get(0);
        ReportEntity second = nonDuplicates.get(1);

        int score1 = calculatePriorityScore(first);
        int score2 = calculatePriorityScore(second);

        // Equal priority score, same timestamp, but contradictory resolution flag or description
        if (score1 == score2 && first.getTimestamp().equals(second.getTimestamp())) {
            return first.isResolved() != second.isResolved()
                    || !first.getDescription().equalsIgnoreCase(second.getDescription());
        }

        return false;
    }

    private int calculatePriorityScore(ReportEntity r) {
        int score = 0;
        if (r.getSource() == ReportSource.MOBILE) score += 10;
        if (r.isResolved()) score += 5;
        return score;
    }

    private ResolutionEntity persistResolution(
            ReportEntity effectiveReport,
            String issueId,
            String resolvedBy,
            ActionTaken actionTaken,
            List<String> evidence,
            Optional<ResolutionEntity> latestResolution
    ) {
        int nextVersion = latestResolution.map(r -> r.getVersion() + 1).orElse(1);
        ResolutionEntity resolution = ResolutionEntity.create();
        resolution.setIssueId(issueId);
        resolution.setVersion(nextVersion);
        resolution.setActionTaken(actionTaken);
        resolution.setResolvedBy(resolvedBy);
        resolution.setLastModified(Instant.now());
        try {
            resolution.setResolution(objectMapper.writeValueAsString(effectiveReport));
            resolution.setEvidence(objectMapper.writeValueAsString(evidence));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize resolution payload", e);
        }
        return resolutionRepository.save(resolution);
    }

    private ReportEntity mapToEntity(ReportRequest request, String identityId, String issueId) {
        ReportEntity entity = ReportEntity.create();
        entity.setReportId(request.reportId());
        entity.setUserId(request.userId());
        entity.setIdentityId(identityId);
        entity.setTimestamp(request.timestamp());
        entity.setLatitude(request.location().lat());
        entity.setLongitude(request.location().lng());
        entity.setCategory(request.category());
        entity.setDescription(request.description());
        entity.setSource(request.parsedSource());
        entity.setDuplicate(request.duplicate());
        entity.setResolved(request.resolved());
        entity.setIssueId(issueId);
        return entity;
    }

    private ReportResponse toIdempotentResponse(ReportEntity report) {
        var latest = resolutionRepository.findTopByIssueIdOrderByVersionDesc(report.getIssueId()).orElseThrow();
        List<String> evidenceList;
        try {
            evidenceList = objectMapper.readValue(latest.getEvidence(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            evidenceList = List.of(report.getReportId());
        }

        return new ReportResponse(
                report.getReportId(),
                report.getIdentityId(),
                report.getIssueId(),
                latest.getActionTaken(),
                latest.getResolvedBy(),
                latest.getLastModified(),
                evidenceList
        );
    }

    // Query Methods for APIs & Dashboard

    @Transactional(readOnly = true)
    public List<ResolutionSummary> getResolutions(String issueId) {
        return resolutionRepository.findByIssueIdOrderByVersionAsc(issueId)
                .stream()
                .map(this::mapToResolutionSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IssueSummary> getAllIssues() {
        List<String> issueIds = resolutionRepository.findDistinctIssueIds();
        List<IssueSummary> summaries = new ArrayList<>();

        for (String issueId : issueIds) {
            Optional<ResolutionEntity> latestOpt = resolutionRepository.findTopByIssueIdOrderByVersionDesc(issueId);
            if (latestOpt.isEmpty()) continue;

            ResolutionEntity latest = latestOpt.get();
            List<ReportEntity> reports = reportRepository.findByIssueId(issueId);
            if (reports.isEmpty()) continue;

            ReportEntity sample = reports.get(reports.size() - 1);
            boolean isResolved = reports.stream().anyMatch(ReportEntity::isResolved);

            summaries.add(new IssueSummary(
                    issueId,
                    sample.getCategory(),
                    sample.getLatitude(),
                    sample.getLongitude(),
                    latest.getVersion(),
                    latest.getActionTaken(),
                    isResolved,
                    reports.size(),
                    latest.getLastModified(),
                    latest.getResolvedBy()
            ));
        }

        summaries.sort(Comparator.comparing(IssueSummary::lastModified).reversed());
        return summaries;
    }

    @Transactional(readOnly = true)
    public List<ReportEntity> getRecentReports() {
        return reportRepository.findAllByOrderByTimestampDesc();
    }

    private ResolutionSummary mapToResolutionSummary(ResolutionEntity entity) {
        List<String> evidenceList;
        try {
            evidenceList = objectMapper.readValue(entity.getEvidence(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            evidenceList = Collections.emptyList();
        }

        return new ResolutionSummary(
                entity.getIssueId(),
                entity.getVersion(),
                entity.getActionTaken(),
                entity.getResolvedBy(),
                entity.getLastModified(),
                evidenceList
        );
    }

    private record ResolutionEvaluation(
            ActionTaken actionTaken,
            ReportEntity effectiveReport,
            List<String> evidence
    ) {}
}
