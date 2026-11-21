-- ============================================================
-- V6: Liquidação de ativos
--
-- * liquidated_at = primeiro mês em que o ativo deixa de existir
--   (sempre dia 1). O ativo aparece nos meses anteriores e não
--   aceita lançamentos a partir desse mês.
-- * A coluna legada "active" continua existindo e é mantida em
--   sincronia pela aplicação (active = liquidated_at IS NULL) apenas
--   para permitir rollback do backend. Remover em uma V7.
-- * Exclusões deixam de apagar histórico em cascata.
-- ============================================================

ALTER TABLE finup.assets
    ADD COLUMN IF NOT EXISTS liquidated_at DATE;

ALTER TABLE finup.assets
    ADD CONSTRAINT ck_assets_liquidated_at_first_day
    CHECK (liquidated_at IS NULL OR EXTRACT(DAY FROM liquidated_at) = 1);

-- Ativos já desativados viram liquidados no mês seguinte ao último
-- lançamento. Sem nenhum lançamento: mês da desativação (updated_at).
UPDATE finup.assets a
SET liquidated_at = COALESCE(
        (SELECT (make_date(e.year, e.month, 1) + INTERVAL '1 month')::date
           FROM finup.monthly_entries e
          WHERE e.asset_id = a.id
          ORDER BY e.year DESC, e.month DESC
          LIMIT 1),
        date_trunc('month', a.updated_at)::date)
WHERE a.active = FALSE;

-- Troca ON DELETE CASCADE por NO ACTION em asset_groups -> assets e
-- assets -> monthly_entries. NO ACTION (e não RESTRICT) para que a
-- cascata a partir de users continue funcionando.
-- Os nomes das FKs são localizados dinamicamente porque foram gerados
-- implicitamente pelo Postgres nas migrations V3/V4.
DO $$
DECLARE
    fk RECORD;
BEGIN
    FOR fk IN
        SELECT con.conname, rel.relname
          FROM pg_constraint con
          JOIN pg_class rel     ON rel.oid = con.conrelid
          JOIN pg_namespace ns  ON ns.oid  = rel.relnamespace
         WHERE ns.nspname = 'finup'
           AND con.contype = 'f'
           AND (   (rel.relname = 'assets'          AND con.confrelid = 'finup.asset_groups'::regclass)
                OR (rel.relname = 'monthly_entries' AND con.confrelid = 'finup.assets'::regclass))
    LOOP
        EXECUTE format('ALTER TABLE finup.%I DROP CONSTRAINT %I', fk.relname, fk.conname);
    END LOOP;
END $$;

ALTER TABLE finup.assets
    ADD CONSTRAINT fk_assets_group
    FOREIGN KEY (group_id) REFERENCES finup.asset_groups (id);

ALTER TABLE finup.monthly_entries
    ADD CONSTRAINT fk_monthly_entries_asset
    FOREIGN KEY (asset_id) REFERENCES finup.assets (id);
