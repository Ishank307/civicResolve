package com.workdna.civic.api.controller;

import com.workdna.civic.api.dto.AuditEntrySummary;
import com.workdna.civic.api.dto.IssueSummary;
import com.workdna.civic.api.dto.ReportRequest;
import com.workdna.civic.api.dto.ReportResponse;
import com.workdna.civic.api.dto.ReplayRequest;
import com.workdna.civic.api.dto.ResolutionSummary;
import com.workdna.civic.domain.model.ReportEntity;
import com.workdna.civic.service.audit.AuditService;
import com.workdna.civic.service.replay.ReplayService;
import com.workdna.civic.service.resolution.ResolutionEngineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ResolutionEngineService resolutionEngineService;
    private final ReplayService replayService;
    private final AuditService auditService;

    public ReportController(
            ResolutionEngineService resolutionEngineService,
            ReplayService replayService,
            AuditService auditService
    ) {
        this.resolutionEngineService = resolutionEngineService;
        this.replayService = replayService;
        this.auditService = auditService;
    }

    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse submitReport(@Valid @RequestBody ReportRequest request) {
        return resolutionEngineService.processReport(request);
    }

    @PostMapping("/replay")
    public List<ReportResponse> replayReports(@Valid @RequestBody ReplayRequest request) {
        return replayService.replay(request.reports());
    }

    @GetMapping("/issues")
    public List<IssueSummary> getIssues() {
        return resolutionEngineService.getAllIssues();
    }

    @GetMapping("/reports")
    public List<ReportEntity> getReports() {
        return resolutionEngineService.getRecentReports();
    }

    @GetMapping("/resolutions/{issueId}")
    public List<ResolutionSummary> getResolutions(@PathVariable String issueId) {
        return resolutionEngineService.getResolutions(issueId);
    }

    @GetMapping("/audit/{issueId}")
    public List<AuditEntrySummary> getAuditTrail(@PathVariable String issueId) {
        return auditService.getAuditTrail(issueId);
    }
}

