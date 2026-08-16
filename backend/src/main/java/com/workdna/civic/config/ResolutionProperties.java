package com.workdna.civic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "civic.resolution")
public record ResolutionProperties(
        int identityWindowMinutes,
        double locationRadiusMeters
) {}
