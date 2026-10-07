-- Datas de criação e última edição das notas
ALTER TABLE notes ADD COLUMN created_at DATETIME(6) NULL;
ALTER TABLE notes ADD COLUMN updated_at DATETIME(6) NULL;

UPDATE notes SET created_at = CURRENT_TIMESTAMP(6), updated_at = CURRENT_TIMESTAMP(6) WHERE created_at IS NULL;
