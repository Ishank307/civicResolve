package com.workdna.civic.service.resolution;

import java.util.List;

public class ConflictResolutionPendingException extends RuntimeException {

    private final String issueId;
    private final List<String> conflictingReports;

    public ConflictResolutionPendingException(String issueId, List<String> conflictingReports, String message) {
        super(message);
        this.issueId = issueId;
        this.conflictingReports = conflictingReports;
    }

    public String getIssueId() {
        return issueId;
    }

    public List<String> getConflictingReports() {
        return conflictingReports;
    }
}
