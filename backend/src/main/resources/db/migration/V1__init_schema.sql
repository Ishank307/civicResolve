-- Reports ingested from web/mobile sources
CREATE TABLE reports (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id       VARCHAR(64)  NOT NULL UNIQUE,
    user_id         VARCHAR(128) NOT NULL,
    identity_id     VARCHAR(128),
    timestamp       TIMESTAMPTZ  NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    category        VARCHAR(64)  NOT NULL,
    description     TEXT         NOT NULL,
    source          VARCHAR(16)  NOT NULL,
    is_duplicate    BOOLEAN      NOT NULL DEFAULT FALSE,
    is_resolved     BOOLEAN      NOT NULL DEFAULT FALSE,
    issue_id        VARCHAR(128),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reports_issue_id ON reports (issue_id);
CREATE INDEX idx_reports_location_category ON reports (category, latitude, longitude);
CREATE INDEX idx_reports_timestamp ON reports (timestamp);

-- Canonical identity mappings
CREATE TABLE identity_mappings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         VARCHAR(128) NOT NULL,
    identity_id     VARCHAR(128) NOT NULL,
    email           VARCHAR(256),
    device_fingerprint VARCHAR(256),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (user_id)
);

CREATE INDEX idx_identity_mappings_identity_id ON identity_mappings (identity_id);

-- Versioned resolution state per issue
CREATE TABLE resolutions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id        VARCHAR(128) NOT NULL,
    version         INTEGER      NOT NULL,
    action_taken    VARCHAR(32)  NOT NULL,
    resolved_by     VARCHAR(128) NOT NULL,
    resolution      JSONB        NOT NULL,
    evidence        JSONB        NOT NULL,
    last_modified   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (issue_id, version)
);

CREATE INDEX idx_resolutions_issue_id ON resolutions (issue_id);

-- Immutable audit trail for every decision
CREATE TABLE audit_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id        VARCHAR(128) NOT NULL,
    action          VARCHAR(64)  NOT NULL,
    input_reports   JSONB        NOT NULL,
    resolved_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    resolved_by     VARCHAR(128) NOT NULL,
    state_before    JSONB,
    state_after     JSONB        NOT NULL
);

CREATE INDEX idx_audit_entries_issue_id ON audit_entries (issue_id);
CREATE INDEX idx_audit_entries_resolved_at ON audit_entries (resolved_at);
