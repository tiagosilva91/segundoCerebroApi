-- Criação da tabela de Temas
CREATE TABLE themes (
    id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    user_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_theme_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Criação da tabela de Notas (Sermões)
CREATE TABLE notes (
    id BINARY(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    audio_url VARCHAR(255),
    image_url VARCHAR(255),
    user_id BINARY(16) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_note_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tabela de relacionamento Muitos-para-Muitos (Note <-> Theme)
CREATE TABLE note_themes (
    note_id BINARY(16) NOT NULL,
    theme_id BINARY(16) NOT NULL,
    PRIMARY KEY (note_id, theme_id),
    CONSTRAINT fk_nt_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    CONSTRAINT fk_nt_theme FOREIGN KEY (theme_id) REFERENCES themes(id) ON DELETE CASCADE
);

-- Tabela para a Element Collection de Referências Bíblicas
CREATE TABLE note_biblical_references (
    note_id BINARY(16) NOT NULL,
    reference VARCHAR(255) NOT NULL,
    CONSTRAINT fk_nbr_note FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE
);