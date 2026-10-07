package com.segundoCerebroApi.service.bible;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

/**
 * Catálogo estático dos 66 livros (cânon protestante), com nomes em PT-BR,
 * abreviações e quantidade de capítulos. O "id" segue a numeração padrão (1–66),
 * a mesma usada pelo provedor bolls.life.
 */
public final class BibleCatalog {

    public enum Testament { AT, NT }

    public record Book(int id, String name, String abbrev, Testament testament, int chapters, List<String> aliases) {
        Book(int id, String name, String abbrev, Testament testament, int chapters, String... aliases) {
            this(id, name, abbrev, testament, chapters, List.of(aliases));
        }
    }

    private BibleCatalog() {}

    public static final List<Book> BOOKS = List.of(
            new Book(1, "Gênesis", "Gn", Testament.AT, 50),
            new Book(2, "Êxodo", "Êx", Testament.AT, 40, "Ex"),
            new Book(3, "Levítico", "Lv", Testament.AT, 27),
            new Book(4, "Números", "Nm", Testament.AT, 36),
            new Book(5, "Deuteronômio", "Dt", Testament.AT, 34),
            new Book(6, "Josué", "Js", Testament.AT, 24),
            new Book(7, "Juízes", "Jz", Testament.AT, 21),
            new Book(8, "Rute", "Rt", Testament.AT, 4),
            new Book(9, "1 Samuel", "1Sm", Testament.AT, 31),
            new Book(10, "2 Samuel", "2Sm", Testament.AT, 24),
            new Book(11, "1 Reis", "1Rs", Testament.AT, 22),
            new Book(12, "2 Reis", "2Rs", Testament.AT, 25),
            new Book(13, "1 Crônicas", "1Cr", Testament.AT, 29),
            new Book(14, "2 Crônicas", "2Cr", Testament.AT, 36),
            new Book(15, "Esdras", "Ed", Testament.AT, 10),
            new Book(16, "Neemias", "Ne", Testament.AT, 13),
            new Book(17, "Ester", "Et", Testament.AT, 10),
            new Book(18, "Jó", "Jó", Testament.AT, 42),
            new Book(19, "Salmos", "Sl", Testament.AT, 150, "Salmo"),
            new Book(20, "Provérbios", "Pv", Testament.AT, 31),
            new Book(21, "Eclesiastes", "Ec", Testament.AT, 12),
            new Book(22, "Cânticos", "Ct", Testament.AT, 8, "Cantares", "Cântico dos Cânticos", "Cantares de Salomão"),
            new Book(23, "Isaías", "Is", Testament.AT, 66),
            new Book(24, "Jeremias", "Jr", Testament.AT, 52),
            new Book(25, "Lamentações", "Lm", Testament.AT, 5, "Lamentações de Jeremias"),
            new Book(26, "Ezequiel", "Ez", Testament.AT, 48),
            new Book(27, "Daniel", "Dn", Testament.AT, 12),
            new Book(28, "Oseias", "Os", Testament.AT, 14, "Oséias"),
            new Book(29, "Joel", "Jl", Testament.AT, 3),
            new Book(30, "Amós", "Am", Testament.AT, 9),
            new Book(31, "Obadias", "Ob", Testament.AT, 1),
            new Book(32, "Jonas", "Jn", Testament.AT, 4),
            new Book(33, "Miqueias", "Mq", Testament.AT, 7, "Miquéias"),
            new Book(34, "Naum", "Na", Testament.AT, 3),
            new Book(35, "Habacuque", "Hc", Testament.AT, 3),
            new Book(36, "Sofonias", "Sf", Testament.AT, 3),
            new Book(37, "Ageu", "Ag", Testament.AT, 2),
            new Book(38, "Zacarias", "Zc", Testament.AT, 14),
            new Book(39, "Malaquias", "Ml", Testament.AT, 4),
            new Book(40, "Mateus", "Mt", Testament.NT, 28),
            new Book(41, "Marcos", "Mc", Testament.NT, 16),
            new Book(42, "Lucas", "Lc", Testament.NT, 24),
            new Book(43, "João", "Jo", Testament.NT, 21),
            new Book(44, "Atos", "At", Testament.NT, 28, "Atos dos Apóstolos"),
            new Book(45, "Romanos", "Rm", Testament.NT, 16),
            new Book(46, "1 Coríntios", "1Co", Testament.NT, 16),
            new Book(47, "2 Coríntios", "2Co", Testament.NT, 13),
            new Book(48, "Gálatas", "Gl", Testament.NT, 6),
            new Book(49, "Efésios", "Ef", Testament.NT, 6),
            new Book(50, "Filipenses", "Fp", Testament.NT, 4),
            new Book(51, "Colossenses", "Cl", Testament.NT, 4),
            new Book(52, "1 Tessalonicenses", "1Ts", Testament.NT, 5),
            new Book(53, "2 Tessalonicenses", "2Ts", Testament.NT, 3),
            new Book(54, "1 Timóteo", "1Tm", Testament.NT, 6),
            new Book(55, "2 Timóteo", "2Tm", Testament.NT, 4),
            new Book(56, "Tito", "Tt", Testament.NT, 3),
            new Book(57, "Filemom", "Fm", Testament.NT, 1, "Filemon"),
            new Book(58, "Hebreus", "Hb", Testament.NT, 13),
            new Book(59, "Tiago", "Tg", Testament.NT, 5),
            new Book(60, "1 Pedro", "1Pe", Testament.NT, 5),
            new Book(61, "2 Pedro", "2Pe", Testament.NT, 3),
            new Book(62, "1 João", "1Jo", Testament.NT, 5),
            new Book(63, "2 João", "2Jo", Testament.NT, 1),
            new Book(64, "3 João", "3Jo", Testament.NT, 1),
            new Book(65, "Judas", "Jd", Testament.NT, 1),
            new Book(66, "Apocalipse", "Ap", Testament.NT, 22)
    );

    public static Optional<Book> findById(int id) {
        if (id < 1 || id > BOOKS.size()) return Optional.empty();
        return Optional.of(BOOKS.get(id - 1));
    }

    /**
     * Resolve um livro a partir de id numérico, nome ou abreviação.
     * A busca é feita em ordem de precisão para desambiguar casos como "Jó" x "Jo" (João):
     * 1) nome/alias/abreviação exatos (ignorando caixa, respeitando acentos);
     * 2) nome/alias normalizados (sem acentos, espaços e pontuação);
     * 3) abreviação normalizada.
     */
    public static Optional<Book> find(String input) {
        if (input == null || input.isBlank()) return Optional.empty();
        String raw = input.trim();

        if (raw.matches("\\d+")) return findById(Integer.parseInt(raw));

        String compact = raw.replaceAll("[\\s.]+", "");
        for (Book b : BOOKS) {
            if (b.name().equalsIgnoreCase(raw) || b.abbrev().equalsIgnoreCase(compact)
                    || b.aliases().stream().anyMatch(a -> a.equalsIgnoreCase(raw))) {
                return Optional.of(b);
            }
        }

        String key = normalize(raw);
        for (Book b : BOOKS) {
            if (normalize(b.name()).equals(key) || b.aliases().stream().anyMatch(a -> normalize(a).equals(key))) {
                return Optional.of(b);
            }
        }
        for (Book b : BOOKS) {
            if (normalize(b.abbrev()).equals(key)) return Optional.of(b);
        }
        return Optional.empty();
    }

    public static String normalize(String s) {
        String noAccents = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noAccents.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
