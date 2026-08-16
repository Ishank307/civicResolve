package com.workdna.civic.api.dto;

import com.workdna.civic.domain.enums.ActionTaken;

import java.time.Instant;
import java.util.List;

public record ResolutionSummary(
        String issueId,
        int version,
        ActionTaken actionTaken,
        String resolvedBy,
        Instant lastModified,
        List<String> evidence
) {}
