-- V5: Add institution column to assets table
ALTER TABLE finup.assets
    ADD COLUMN IF NOT EXISTS institution VARCHAR(150);
