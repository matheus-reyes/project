CREATE TABLE short_links (
    short_code   TEXT PRIMARY KEY,
    original_url TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    expires_at   TIMESTAMPTZ
);

CREATE TABLE clicks (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    short_code TEXT        NOT NULL REFERENCES short_links (short_code),
    clicked_at TIMESTAMPTZ NOT NULL,
    referrer   TEXT        NOT NULL,
    user_agent TEXT,
    ip         TEXT
);

-- Stats and click counts always filter by link
CREATE INDEX clicks_short_code_clicked_at_idx ON clicks (short_code, clicked_at);
