package com.workdna.civic.service.identity;

import com.workdna.civic.api.dto.ReportRequest;
import com.workdna.civic.config.ResolutionProperties;
import com.workdna.civic.domain.model.IdentityMappingEntity;
import com.workdna.civic.domain.model.ReportEntity;
import com.workdna.civic.repository.IdentityMappingRepository;
import com.workdna.civic.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdentityResolutionService {

    private final IdentityMappingRepository identityMappingRepository;
    private final ReportRepository reportRepository;
    private final ResolutionProperties properties;

    public IdentityResolutionService(
            IdentityMappingRepository identityMappingRepository,
            ReportRepository reportRepository,
            ResolutionProperties properties
    ) {
        this.identityMappingRepository = identityMappingRepository;
        this.reportRepository = reportRepository;
        this.properties = properties;
    }

    @Transactional
    public String resolveIdentity(ReportRequest request) {
        // 1. Same user ID
        Optional<IdentityMappingEntity> existingByUser =
                identityMappingRepository.findByUserId(request.userId());
        if (existingByUser.isPresent()) {
            return existingByUser.get().getIdentityId();
        }

        // 2. Same email
        if (request.email() != null && !request.email().isBlank()) {
            Optional<IdentityMappingEntity> byEmail =
                    identityMappingRepository.findByEmail(request.email());
            if (byEmail.isPresent()) {
                return linkUser(request, byEmail.get().getIdentityId());
            }
        }

        // 3. Same device fingerprint
        if (request.deviceFingerprint() != null && !request.deviceFingerprint().isBlank()) {
            Optional<IdentityMappingEntity> byDevice =
                    identityMappingRepository.findByDeviceFingerprint(request.deviceFingerprint());
            if (byDevice.isPresent()) {
                return linkUser(request, byDevice.get().getIdentityId());
            }
        }

        // 4. Same location + timestamp + category within identity window (default 10 min, <= 50m)
        int windowMinutes = properties != null && properties.identityWindowMinutes() > 0
                ? properties.identityWindowMinutes()
                : 10;
        double radiusMeters = properties != null && properties.locationRadiusMeters() > 0
                ? properties.locationRadiusMeters()
                : 50.0;

        Instant start = request.timestamp().minus(Duration.ofMinutes(windowMinutes));
        Instant end = request.timestamp().plus(Duration.ofMinutes(windowMinutes));

        List<ReportEntity> nearbyCandidates = reportRepository.findByCategoryAndTimestampBetween(
                request.category(),
                start,
                end
        );

        for (ReportEntity candidate : nearbyCandidates) {
            double distance = calculateDistanceMeters(
                    request.location().lat(),
                    request.location().lng(),
                    candidate.getLatitude(),
                    candidate.getLongitude()
            );

            if (distance <= radiusMeters && candidate.getIdentityId() != null && !candidate.getIdentityId().isBlank()) {
                return linkUser(request, candidate.getIdentityId());
            }
        }

        // 5. Create new canonical identity
        String identityId = UUID.randomUUID().toString();
        persistMapping(request, identityId);
        return identityId;
    }

    private String linkUser(ReportRequest request, String identityId) {
        persistMapping(request, identityId);
        return identityId;
    }

    private void persistMapping(ReportRequest request, String identityId) {
        IdentityMappingEntity mapping = IdentityMappingEntity.of(request.userId(), identityId);
        if (request.email() != null && !request.email().isBlank()) {
            mapping.setEmail(request.email());
        }
        if (request.deviceFingerprint() != null && !request.deviceFingerprint().isBlank()) {
            mapping.setDeviceFingerprint(request.deviceFingerprint());
        }
        identityMappingRepository.save(mapping);
    }

    private double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

