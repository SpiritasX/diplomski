CREATE TABLE refresh_sessions (
    session_id      RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    student_index   VARCHAR2(20) NOT NULL REFERENCES accounts(student_index),
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    last_used_at    TIMESTAMP WITH TIME ZONE,
    revoked_at      TIMESTAMP WITH TIME ZONE,
    revoke_reason   VARCHAR2(100),

    CONSTRAINT chk_refresh_session_dates
        CHECK (expires_at > created_at),
    CONSTRAINT chk_refresh_session_revoke_reason
        CHECK (revoked_at IS NOT NULL OR revoke_reason IS NULL)
);

CREATE TABLE refresh_tokens (
    token_id     RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    session_id   RAW(16) NOT NULL REFERENCES refresh_sessions(session_id),
    token_hash   VARCHAR2(64) NOT NULL,
    status       VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,
    issued_at    TIMESTAMP WITH TIME ZONE DEFAULT SYSTIMESTAMP NOT NULL,
    used_at      TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uq_refresh_token_hash
        UNIQUE (token_hash),
    CONSTRAINT chk_refresh_token_status
        CHECK (status IN ('ACTIVE', 'USED', 'REVOKED')),
    CONSTRAINT chk_refresh_token_used_at
        CHECK ((status = 'USED' AND used_at IS NOT NULL) OR (status <> 'USED' AND used_at IS NULL))
);
