-- Cria o schema dedicado para o FinUp
CREATE SCHEMA IF NOT EXISTS finup;

-- Tabela de usuários
CREATE TABLE finup.users (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100)        NOT NULL,
    email      VARCHAR(150)        NOT NULL UNIQUE,
    password   VARCHAR(255)        NOT NULL,
    active     BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_finup_users_email ON finup.users (email);
