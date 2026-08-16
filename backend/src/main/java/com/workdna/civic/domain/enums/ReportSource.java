package com.workdna.civic.domain.enums;

public enum ReportSource {
    WEB,
    MOBILE;

    public static ReportSource fromValue(String value) {
        return ReportSource.valueOf(value.trim().toUpperCase());
    }
}
