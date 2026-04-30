-- Alteração dos UUIDs de BINARY(16) para VARCHAR(36) para consistência com a tabela users
-- e evitar problemas de compatibilidade entre Hibernate e MySQL.

-- 1. Drop das Foreign Keys que referenciam as colunas que serão alteradas
ALTER TABLE note_themes DROP FOREIGN KEY fk_nt_note;
ALTER TABLE note_themes DROP FOREIGN KEY fk_nt_theme;
ALTER TABLE note_biblical_references DROP FOREIGN KEY fk_nbr_note;

-- 2. Alteração dos tipos das colunas
ALTER TABLE themes MODIFY id VARCHAR(36) NOT NULL;
ALTER TABLE notes MODIFY id VARCHAR(36) NOT NULL;

ALTER TABLE note_themes MODIFY note_id VARCHAR(36) NOT NULL;
ALTER TABLE note_themes MODIFY theme_id VARCHAR(36) NOT NULL;

ALTER TABLE note_biblical_references MODIFY note_id VARCHAR(36) NOT NULL;

-- 3. Recriação das Foreign Keys
ALTER TABLE note_themes ADD CONSTRAINT fk_nt_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE;
ALTER TABLE note_themes ADD CONSTRAINT fk_nt_theme FOREIGN KEY (theme_id) REFERENCES themes(id) ON DELETE CASCADE;
ALTER TABLE note_biblical_references ADD CONSTRAINT fk_nbr_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE;
