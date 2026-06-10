CREATE TABLE finup.asset_groups (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT              NOT NULL REFERENCES finup.users (id) ON DELETE CASCADE,
    name          VARCHAR(100)        NOT NULL,
    display_order INT                 NOT NULL DEFAULT 0,
    created_at    TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_finup_asset_groups_user ON finup.asset_groups (user_id);
