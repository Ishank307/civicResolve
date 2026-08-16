package com.workdna.civic.api.dto;

import com.workdna.civic.domain.enums.ActionTaken;

import java.time.Instant;

public record IssueSummary(
        String issueId,
        String category,
        double latitude,
        double longitude,
        int latestVersion,
        ActionTaken actionTaken,
        boolean isResolved,
        long reportCount,
        Instant lastModified,
        String resolvedBy
) {}
