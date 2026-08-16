package com.workdna.civic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workdna.civic.api.dto.LocationDto;
import com.workdna.civic.api.dto.ReportRequest;
import com.workdna.civic.api.dto.ReportResponse;
import com.workdna.civic.domain.enums.ActionTaken;
import com.workdna.civic.service.replay.ReplayService;
import com.workdna.civic.service.resolution.ResolutionEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ResolutionEngineIntegrationTest {

    @Autowired
    private ResolutionEngineService resolutionEngineService;

    @Autowired
    private ReplayService replayService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.workdna.civic.repository.ReportRepository reportRepository;

    @Autowired
    private com.workdna.civic.repository.ResolutionRepository resolutionRepository;

    @Autowired
    private com.workdna.civic.repository.IdentityMappingRepository identityMappingRepository;

    @Autowired
    private com.workdna.civic.repository.AuditEntryRepository auditEntryRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        auditEntryRepository.deleteAll();
        resolutionRepository.deleteAll();
        reportRepository.deleteAll();
        identityMappingRepository.deleteAll();
    }

    @Test
    void testIdentityResolutionEmailAndDeviceLinking() {
        ReportRequest report1 = new ReportRequest(
                "r-id-1",
                "user-alice-desk",
                Instant.parse("2026-01-15T10:00:00Z"),
                new LocationDto(28.6139, 77.2090),
                "pothole",
                "Pothole on main rd",
                "web",
                false,
                false,
                "alice@example.com",
                null
        );
        ReportResponse res1 = resolutionEngineService.processReport(report1);

        ReportRequest report2 = new ReportRequest(
                "r-id-2",
                "user-alice-phone",
                Instant.parse("2026-01-15T10:02:00Z"),
                new LocationDto(28.6139, 77.2090),
                "pothole",
                "Pothole verified",
                "mobile",
                false,
                false,
                "alice@example.com",
                "device-fingerprint-999"
        );
        ReportResponse res2 = resolutionEngineService.processReport(report2);

        // Same email must resolve to the identical identityId
        assertThat(res2.identityId()).isEqualTo(res1.identityId());

        ReportRequest report3 = new ReportRequest(
                "r-id-3",
                "user-alice-other",
                Instant.parse("2026-01-15T10:03:00Z"),
                new LocationDto(28.6139, 77.2090),
                "pothole",
                "Third report",
                "web",
                false,
                false,
                null,
                "device-fingerprint-999"
        );
        ReportResponse res3 = resolutionEngineService.processReport(report3);

        // Same device fingerprint must resolve to the identical identityId
        assertThat(res3.identityId()).isEqualTo(res1.identityId());
    }

    @Test
    void testSpatioTemporalIdentityResolution() {
        ReportRequest report1 = new ReportRequest(
                "r-st-1",
                "user-1",
                Instant.parse("2026-01-15T10:00:00Z"),
                new LocationDto(28.6139, 77.2090),
                "streetlight",
                "Streetlight outage",
                "mobile",
                false,
                false,
                null,
                null
        );
        ReportResponse res1 = resolutionEngineService.processReport(report1);

        // Different user ID, no email/device, but within 2 minutes and 20 meters, same category
        ReportRequest report2 = new ReportRequest(
                "r-st-2",
                "user-unknown-nearby",
                Instant.parse("2026-01-15T10:02:00Z"),
                new LocationDto(28.61391, 77.20902),
                "streetlight",
                "Dark street",
                "web",
                false,
                false,
                null,
                null
        );
        ReportResponse res2 = resolutionEngineService.processReport(report2);

        assertThat(res2.identityId()).isEqualTo(res1.identityId());
    }

    @Test
    void testIdempotency() {
        ReportRequest report = new ReportRequest(
                "r-idempotent-1",
                "user-single",
                Instant.parse("2026-01-15T10:00:00Z"),
                new LocationDto(28.6139, 77.2090),
                "drainage",
                "Blocked drain",
                "mobile",
                false,
                false,
                null,
                null
        );

        ReportResponse first = resolutionEngineService.processReport(report);
        ReportResponse second = resolutionEngineService.processReport(report);

        assertThat(second.reportId()).isEqualTo(first.reportId());
        assertThat(second.issueId()).isEqualTo(first.issueId());
        assertThat(second.actionTaken()).isEqualTo(first.actionTaken());
        assertThat(second.identityId()).isEqualTo(first.identityId());
    }

    @Test
    void testDuplicateReportDetection() {
        ReportRequest report1 = new ReportRequest(
                "r-dup-1",
                "user-1",
                Instant.parse("2026-01-15T10:00:00Z"),
                new LocationDto(28.6139, 77.2090),
                "traffic_sign",
                "Broken stop sign",
                "web",
                false,
                false,
                null,
                null
        );
        ReportResponse res1 = resolutionEngineService.processReport(report1);
        assertThat(res1.actionTaken()).isEqualTo(ActionTaken.NEW_ISSUE);

        ReportRequest report2 = new ReportRequest(
                "r-dup-2",
                "user-2",
                Instant.parse("2026-01-15T10:02:00Z"),
                new LocationDto(28.6139, 77.2090),
                "traffic_sign",
                "Broken stop sign duplicate",
                "web",
                true, // duplicate flag
                false,
                null,
                null
        );
        ReportResponse res2 = resolutionEngineService.processReport(report2);
        assertThat(res2.actionTaken()).isEqualTo(ActionTaken.DUPLICATE);
    }

    @Test
    void testPrecedenceAndResolutionOverride() {
        ReportRequest reportWebUnresolved = new ReportRequest(
                "r-prec-1",
                "user-1",
                Instant.parse("2026-01-15T10:00:00Z"),
                new LocationDto(28.6139, 77.2090),
                "graffiti",
                "Graffiti on wall",
                "web",
                false,
                false,
                null,
                null
        );
        ReportResponse res1 = resolutionEngineService.processReport(reportWebUnresolved);
        assertThat(res1.actionTaken()).isEqualTo(ActionTaken.NEW_ISSUE);

        ReportRequest reportMobileResolved = new ReportRequest(
                "r-prec-2",
                "officer-1",
                Instant.parse("2026-01-15T10:05:00Z"),
                new LocationDto(28.6139, 77.2090),
                "graffiti",
                "Graffiti cleaned and painted over",
                "mobile",
                false,
                true, // resolved = true
                null,
                null
        );
        ReportResponse res2 = resolutionEngineService.processReport(reportMobileResolved);
        assertThat(res2.actionTaken()).isEqualTo(ActionTaken.REFINED);
    }

    @Test
    void testSampleFixturesReplayDeterminism() throws Exception {
        InputStream is = new ClassPathResource("fixtures/sample-reports.json").getInputStream();
        List<ReportRequest> sampleReports = objectMapper.readValue(is, new TypeReference<List<ReportRequest>>() {});

        List<ReportResponse> responses = replayService.replay(sampleReports);
        assertThat(responses).hasSize(sampleReports.size());

        // Check query endpoints
        var issues = resolutionEngineService.getAllIssues();
        assertThat(issues).isNotEmpty();

        var recentReports = resolutionEngineService.getRecentReports();
        assertThat(recentReports).hasSize(sampleReports.size());
    }
}
