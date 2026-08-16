package com.workdna.civic.service.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workdna.civic.api.dto.AuditEntrySummary;
import com.workdna.civic.domain.model.AuditEntryEntity;
import com.workdna.civic.domain.model.ResolutionEntity;
import com.workdna.civic.repository.AuditEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class AuditService {

    private final AuditEntryRepository auditEntryRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEntryRepository auditEntryRepository, ObjectMapper objectMapper) {
        this.auditEntryRepository = auditEntryRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordDecision(
            String issueId,
            String action,
            List<String> inputReports,
            String resolvedBy,
            ResolutionEntity stateBefore,
            ResolutionEntity stateAfter
    ) {
        AuditEntryEntity entry = AuditEntryEntity.create();
        entry.setIssueId(issueId);
        entry.setAction(action);
        entry.setResolvedBy(resolvedBy);
        entry.setResolvedAt(Instant.now());
        try {
            entry.setInputReports(objectMapper.writeValueAsString(inputReports));
            if (stateBefore != null) {
                entry.setStateBefore(objectMapper.writeValueAsString(stateBefore));
            }
            if (stateAfter != null) {
                entry.setStateAfter(objectMapper.writeValueAsString(stateAfter));
            } else {
                entry.setStateAfter("{}");
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize audit entry", e);
        }
        auditEntryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditEntrySummary> getAuditTrail(String issueId) {
        return auditEntryRepository.findByIssueIdOrderByResolvedAtAsc(issueId)
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    private AuditEntrySummary mapToSummary(AuditEntryEntity entity) {
        List<String> reports;
        try {
            reports = objectMapper.readValue(entity.getInputReports(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            reports = Collections.emptyList();
        }
        return new AuditEntrySummary(
                entity.getIssueId(),
                entity.getAction(),
                reports,
                entity.getResolvedAt(),
                entity.getResolvedBy(),
                entity.getStateBefore(),
                entity.getStateAfter()
        );
    }
}

