CREATE TABLE finup.monthly_entries (
    id         BIGSERIAL PRIMARY KEY,
    asset_id   BIGINT          NOT NULL REFERENCES finup.assets (id) ON DELETE CASCADE,
    user_id    BIGINT          NOT NULL REFERENCES finup.users (id) ON DELETE CASCADE,
    year       SMALLINT        NOT NULL,
    month      SMALLINT        NOT NULL CHECK (month BETWEEN 1 AND 12),
    balance    NUMERIC(18, 2)  NOT NULL,
    notes      VARCHAR(500),
    created_at TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_monthly_entry UNIQUE (asset_id, year, month)
);

CREATE INDEX idx_finup_entries_user_period ON finup.monthly_entries (user_id, year, month);
CREATE INDEX idx_finup_entries_asset       ON finup.monthly_entries (asset_id);
