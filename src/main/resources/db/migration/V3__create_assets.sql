CREATE TABLE finup.assets (
    id            BIGSERIAL PRIMARY KEY,
    group_id      BIGINT              NOT NULL REFERENCES finup.asset_groups (id) ON DELETE CASCADE,
    user_id       BIGINT              NOT NULL REFERENCES finup.users (id) ON DELETE CASCADE,
    name          VARCHAR(150)        NOT NULL,
    description   VARCHAR(300),
    active        BOOLEAN             NOT NULL DEFAULT TRUE,
    display_order INT                 NOT NULL DEFAULT 0,
    created_at    TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_finup_assets_group ON finup.assets (group_id);
CREATE INDEX idx_finup_assets_user  ON finup.assets (user_id);
