package com.tugatobrasa.api.translation;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.tugatobrasa.api.glossary.GlossaryEntry;
import com.tugatobrasa.api.glossary.GlossaryFuzzyLookup;

// último passo do pipeline: nunca é erro, sempre devolve sugestões + convite de contribuição
@Component
public class NotFoundHandler {

    // mais frouxo que o threshold de resolução (0.4) — aqui é só "você quis dizer?", não uma tradução aceita
    private static final double SUGGESTION_THRESHOLD = 0.2;
    private static final String CONTRIBUTE_BASE_URL =
            "https://github.com/carloseorsantos/tugatobrasa-api/issues/new?template=novo-termo.yml";

    private final GlossaryFuzzyLookup fuzzyLookup;
    private final JdbcTemplate jdbcTemplate;

    public NotFoundHandler(GlossaryFuzzyLookup fuzzyLookup, JdbcTemplate jdbcTemplate) {
        this.fuzzyLookup = fuzzyLookup;
        this.jdbcTemplate = jdbcTemplate;
    }

    public NotResolved handle(String rawInput, String normalizedTerm, Direction direction) {
        List<String> suggestions = findSuggestions(normalizedTerm, direction);
        String contributeUrl = buildContributeUrl(rawInput);

        logNotFound(rawInput, suggestions);

        return new NotResolved(suggestions, contributeUrl);
    }

    private List<String> findSuggestions(String term, Direction direction) {
        List<GlossaryEntry> matches = direction == Direction.PT_TO_BR
                ? fuzzyLookup.findSimilarByTermPt(term, SUGGESTION_THRESHOLD)
                : fuzzyLookup.findSimilarByTermBr(term, SUGGESTION_THRESHOLD);

        return matches.stream()
                .map(e -> direction == Direction.PT_TO_BR ? e.termPt() : e.termBr())
                .distinct()
                .toList();
    }

    private String buildContributeUrl(String rawInput) {
        return CONTRIBUTE_BASE_URL + "&termo=" + URLEncoder.encode(rawInput, StandardCharsets.UTF_8);
    }

    private void logNotFound(String input, List<String> suggestions) {
        jdbcTemplate.update(
                "INSERT INTO translation_log (input, suggestion) VALUES (?, ?)",
                input, String.join(", ", suggestions));
    }
}
