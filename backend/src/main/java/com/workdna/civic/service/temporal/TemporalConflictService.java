package com.workdna.civic.service.temporal;

import com.workdna.civic.api.dto.ReportRequest;
import com.workdna.civic.domain.model.ResolutionEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TemporalConflictService {

    public boolean isLateReport(ReportRequest request, Optional<ResolutionEntity> latestResolution) {
        return latestResolution
                .map(resolution -> request.timestamp().isBefore(resolution.getLastModified()))
                .orElse(false);
    }

    public boolean requiresReplay(ReportRequest request, Optional<ResolutionEntity> latestResolution) {
        return isLateReport(request, latestResolution);
    }
}

