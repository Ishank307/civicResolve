package com.workdna.civic.api.dto;

import java.time.Instant;
import java.util.List;

public record AuditEntrySummary(
        String issueId,
        String action,
        List<String> inputReports,
        Instant resolvedAt,
        String resolvedBy,
        String stateBefore,
        String stateAfter
) {}

