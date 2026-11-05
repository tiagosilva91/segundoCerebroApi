package com.segundoCerebroApi.service.bible;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpreta e normaliza as referências bíblicas gravadas nas notas.
 *
 * <p>O campo era texto livre: aceitava {@code "<b>João</b> 3:16"} ou um livro
 * inexistente sem reclamar, e o erro só aparecia — quebrado — na hora da
 * leitura. Agora a referência é conferida contra o {@link BibleCatalog}, que já
 * existia no projeto, e gravada numa forma canônica ("João 3:16"), de modo que
 * a mesma passagem digitada de jeitos diferentes ("jo 3.16", "JOAO 3:16") fique
 * idêntica no acervo e a busca por referência encontre todas.</p>
 */
public final class BibleReferences {

    private BibleReferences() {}

    /** "Livro C", "Livro C:V" ou "Livro C:V-V2" — separador de versículo ":" ou ".". */
    private static final Pattern REFERENCIA =
            Pattern.compile("^(.+?)\\s+(\\d{1,3})(?:\\s*[:.]\\s*(\\d{1,3})(?:\\s*-\\s*(\\d{1,3}))?)?$");

    /**
     * Devolve a referência na forma canônica.
     *
     * @throws IllegalArgumentException se o formato, o livro, o capítulo ou o
     *         intervalo de versículos não fizerem sentido. O
     *         {@code GlobalExceptionHandler} converte em 400.
     */
    public static String normalizar(String entrada) {
        if (entrada == null || entrada.isBlank()) {
            throw new IllegalArgumentException("Referência bíblica vazia.");
        }

        String texto = entrada.trim().replaceAll("\\s+", " ");

        Matcher m = REFERENCIA.matcher(texto);
        if (!m.matches()) {
            throw new IllegalArgumentException(
                    "Referência bíblica inválida: \"" + entrada + "\". Use o formato \"João 3:16\" ou \"João 3:16-18\".");
        }

        String nomeLivro = m.group(1);
        BibleCatalog.Book livro = BibleCatalog.find(nomeLivro)
                .orElseThrow(() -> new IllegalArgumentException("Livro bíblico desconhecido: \"" + nomeLivro.trim() + "\"."));

        int capitulo = Integer.parseInt(m.group(2));
        if (capitulo < 1 || capitulo > livro.chapters()) {
            throw new IllegalArgumentException(
                    livro.name() + " possui capítulos de 1 a " + livro.chapters() + ".");
        }

        if (m.group(3) == null) {
            return livro.name() + " " + capitulo;
        }

        int versiculoInicial = Integer.parseInt(m.group(3));
        if (versiculoInicial < 1) {
            throw new IllegalArgumentException("Versículo inicial inválido em \"" + entrada + "\".");
        }

        if (m.group(4) == null) {
            return livro.name() + " " + capitulo + ":" + versiculoInicial;
        }

        int versiculoFinal = Integer.parseInt(m.group(4));
        if (versiculoFinal < versiculoInicial) {
            throw new IllegalArgumentException(
                    "Intervalo de versículos invertido em \"" + entrada + "\".");
        }
        return livro.name() + " " + capitulo + ":" + versiculoInicial + "-" + versiculoFinal;
    }
}
