package com.segundoCerebroApi.service.bible;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Versões em português disponíveis no provedor (bolls.life). */
public final class BibleTranslations {

    public record Translation(String code, String name) {}

    private BibleTranslations() {}

    public static final List<Translation> ALL = List.of(
            new Translation("ACF11", "Almeida Corrigida Fiel (2011)"),
            new Translation("ARA", "Almeida Revista e Atualizada (1993)"),
            new Translation("ARC09", "Almeida Revista e Corrigida (2009)"),
            new Translation("NAA", "Nova Almeida Atualizada (2017)"),
            new Translation("NVT", "Nova Versão Transformadora (2016)"),
            new Translation("NTLH", "Nova Tradução na Linguagem de Hoje (2000)"),
            new Translation("TB10", "Tradução Brasileira (2010)"),
            new Translation("KJA", "King James Atualizada (2001)")
    );

    /** Apelidos amigáveis → código do provedor. */
    private static final Map<String, String> ALIASES = Map.of(
            "ACF", "ACF11",
            "ARC", "ARC09",
            "TB", "TB10",
            "ALMEIDA", "ACF11"
    );

    public static Optional<Translation> find(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        String upper = code.trim().toUpperCase();
        String resolved = ALIASES.getOrDefault(upper, upper);
        return ALL.stream().filter(t -> t.code().equals(resolved)).findFirst();
    }
}
