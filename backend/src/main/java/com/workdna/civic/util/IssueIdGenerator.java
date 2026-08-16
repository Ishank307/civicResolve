package com.workdna.civic.util;

import com.workdna.civic.api.dto.ReportRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Component
public class IssueIdGenerator {

    public String generate(ReportRequest request) {
        long windowMinutes = ChronoUnit.MINUTES.between(
                request.timestamp().truncatedTo(ChronoUnit.HOURS),
                request.timestamp()
        ) / 10;

        String raw = String.format(
                "%.4f|%.4f|%s|%d",
                request.location().lat(),
                request.location().lng(),
                request.category(),
                windowMinutes
        );

        return "issue-" + sha256(raw).substring(0, 16);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
