package com.workdna.civic.util;

import com.workdna.civic.api.dto.LocationDto;
import com.workdna.civic.api.dto.ReportRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class IssueIdGeneratorTest {

    private final IssueIdGenerator generator = new IssueIdGenerator();

    @Test
    void generatesDeterministicIssueIdForSameInput() {
        ReportRequest request = sampleRequest("r1");
        assertThat(generator.generate(request)).isEqualTo(generator.generate(request));
    }

    private ReportRequest sampleRequest(String reportId) {
        return new ReportRequest(
                reportId,
                "user-1",
                Instant.parse("2026-01-15T10:05:00Z"),
                new LocationDto(28.6139, 77.2090),
                "pothole",
                "Large pothole near intersection",
                "mobile",
                false,
                false,
                null,
                null
        );
    }
}
