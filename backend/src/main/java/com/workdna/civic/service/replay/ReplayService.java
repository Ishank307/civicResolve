package com.workdna.civic.service.replay;

import com.workdna.civic.api.dto.ReportRequest;
import com.workdna.civic.api.dto.ReportResponse;
import com.workdna.civic.service.resolution.ResolutionEngineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ReplayService {

    private final ResolutionEngineService resolutionEngineService;

    public ReplayService(ResolutionEngineService resolutionEngineService) {
        this.resolutionEngineService = resolutionEngineService;
    }

    @Transactional
    public List<ReportResponse> replay(List<ReportRequest> reports) {
        return reports.stream()
                .sorted(Comparator.comparing(ReportRequest::timestamp))
                .map(resolutionEngineService::processReport)
                .toList();
    }
}
