-- Reports ingested from web/mobile sources
CREATE TABLE reports (
    id              UUID PRIMARY KEY,
    report_id       VARCHAR(64)  NOT NULL UNIQUE,
    user_id         VARCHAR(128) NOT NULL,
    identity_id     VARCHAR(128),
    timestamp       TIMESTAMP WITH TIME ZONE NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    category        VARCHAR(64)  NOT NULL,
    description     TEXT         NOT NULL,
    source          VARCHAR(16)  NOT NULL,
    is_duplicate    BOOLEAN      NOT NULL DEFAULT FALSE,
    is_resolved     BOOLEAN      NOT NULL DEFAULT FALSE,
    issue_id        VARCHAR(128),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reports_issue_id ON reports (issue_id);
CREATE INDEX idx_reports_location_category ON reports (category, latitude, longitude);
CREATE INDEX idx_reports_timestamp ON reports (timestamp);

CREATE TABLE identity_mappings (
    id                  UUID PRIMARY KEY,
    user_id             VARCHAR(128) NOT NULL,
    identity_id         VARCHAR(128) NOT NULL,
    email               VARCHAR(256),
    device_fingerprint  VARCHAR(256),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id)
);

CREATE INDEX idx_identity_mappings_identity_id ON identity_mappings (identity_id);

CREATE TABLE resolutions (
    id              UUID PRIMARY KEY,
    issue_id        VARCHAR(128) NOT NULL,
    version         INTEGER      NOT NULL,
    action_taken    VARCHAR(32)  NOT NULL,
    resolved_by     VARCHAR(128) NOT NULL,
    resolution      TEXT         NOT NULL,
    evidence        TEXT         NOT NULL,
    last_modified   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (issue_id, version)
);

CREATE INDEX idx_resolutions_issue_id ON resolutions (issue_id);

CREATE TABLE audit_entries (
    id              UUID PRIMARY KEY,
    issue_id        VARCHAR(128) NOT NULL,
    action          VARCHAR(64)  NOT NULL,
    input_reports   TEXT         NOT NULL,
    resolved_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_by     VARCHAR(128) NOT NULL,
    state_before    TEXT,
    state_after     TEXT         NOT NULL
);

CREATE INDEX idx_audit_entries_issue_id ON audit_entries (issue_id);
CREATE INDEX idx_audit_entries_resolved_at ON audit_entries (resolved_at);
