package com.workdna.civic.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_entries")
public class AuditEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "issue_id", nullable = false, length = 128)
    private String issueId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(name = "input_reports", nullable = false, columnDefinition = "jsonb")
    private String inputReports;

    @Column(name = "resolved_at", nullable = false)
    private Instant resolvedAt = Instant.now();

    @Column(name = "resolved_by", nullable = false, length = 128)
    private String resolvedBy;

    @Column(name = "state_before", columnDefinition = "jsonb")
    private String stateBefore;

    @Column(name = "state_after", nullable = false, columnDefinition = "jsonb")
    private String stateAfter;

    public AuditEntryEntity() {
    }

    public static AuditEntryEntity create() {
        return new AuditEntryEntity();
    }

    public UUID getId() {
        return id;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getInputReports() {
        return inputReports;
    }

    public void setInputReports(String inputReports) {
        this.inputReports = inputReports;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getStateBefore() {
        return stateBefore;
    }

    public void setStateBefore(String stateBefore) {
        this.stateBefore = stateBefore;
    }

    public String getStateAfter() {
        return stateAfter;
    }

    public void setStateAfter(String stateAfter) {
        this.stateAfter = stateAfter;
    }
}
