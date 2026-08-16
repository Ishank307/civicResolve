package com.workdna.civic.api.dto;

import com.workdna.civic.domain.enums.ActionTaken;

import java.time.Instant;
import java.util.List;

public record ReportResponse(
        String reportId,
        String identityId,
        String issueId,
        ActionTaken actionTaken,
        String resolvedBy,
        Instant resolutionTimestamp,
        List<String> evidence
) {}
