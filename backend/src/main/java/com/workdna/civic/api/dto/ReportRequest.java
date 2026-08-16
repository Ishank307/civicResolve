package com.workdna.civic.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.workdna.civic.domain.enums.ReportSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ReportRequest(
        @NotBlank String reportId,
        @NotBlank String userId,
        @NotNull Instant timestamp,
        @NotNull @Valid LocationDto location,
        @NotBlank String category,
        @NotBlank String description,
        @NotBlank String source,
        @JsonProperty("isDuplicate") boolean duplicate,
        @JsonProperty("isResolved") boolean resolved,
        String email,
        String deviceFingerprint
) {
    public ReportSource parsedSource() {
        return ReportSource.fromValue(source);
    }
}
