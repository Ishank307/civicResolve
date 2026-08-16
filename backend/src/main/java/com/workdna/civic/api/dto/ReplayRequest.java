package com.workdna.civic.api.dto;

import java.util.List;

public record ReplayRequest(
        List<ReportRequest> reports
) {}
