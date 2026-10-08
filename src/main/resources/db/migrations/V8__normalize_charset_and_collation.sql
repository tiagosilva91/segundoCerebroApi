-- Uniformiza charset e colação de todas as tabelas.
--
-- Contexto: V1 declara CHARSET=utf8mb4 em "users", o que resolve para a colação
-- padrão do servidor (utf8mb4_0900_ai_ci no MySQL 8). V2 cria "themes" e "notes"
-- sem declarar nada, herdando a colação padrão do BANCO. Quando as duas diferem,
-- as foreign keys VARCHAR entre elas falham com:
--
--   Referencing column 'user_id' and referenced column 'id' in foreign key
--   constraint 'fk_theme_user' are incompatible.
--
-- Isso derruba o boot inteiro em qualquer banco cuja colação padrão não seja a do
-- servidor MySQL 8 — foi exatamente o que aconteceu em 2026-10-08.
--
-- Esta migration torna o estado explícito em vez de depender do default do ambiente.
-- Migrations futuras devem declarar CHARSET e COLLATE em cada CREATE TABLE.

ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Converter uma coluna referenciada por foreign key falha com as checagens ativas,
-- pois durante a conversão os dois lados ficam momentaneamente incompatíveis.
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE users
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE themes
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE notes
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE note_themes
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE note_biblical_references
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

ALTER TABLE password_reset_tokens
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
