package com.tugatobrasa.api.translation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tugatobrasa.api.glossary.GlossaryEntry;
import com.tugatobrasa.api.glossary.GlossaryFuzzyLookup;
import com.tugatobrasa.api.glossary.GlossaryIndex;

class GlossaryResolverTest {

    private GlossaryResolver resolver;

    @BeforeEach
    void setUp() {
        GlossaryIndex index = new GlossaryIndex();
        index.reload(List.of(
                entry("autocarro", "ônibus", false),
                entry("propina", "mensalidade", true),
                entry("propina", "suborno", true)));
        resolver = new GlossaryResolver(index, mock(GlossaryFuzzyLookup.class));
    }

    @Test
    void resolvesExactMatchPtToBr() {
        List<GlossaryEntry> result = resolver.resolveExact("autocarro", Direction.PT_TO_BR);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).termBr()).isEqualTo("ônibus");
    }

    @Test
    void resolvesExactMatchBrToPt() {
        List<GlossaryEntry> result = resolver.resolveExact("ônibus", Direction.BR_TO_PT);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).termPt()).isEqualTo("autocarro");
    }

    @Test
    void returnsAllSensesWhenTermHasMultiplePairs() {
        List<GlossaryEntry> result = resolver.resolveExact("propina", Direction.PT_TO_BR);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(GlossaryEntry::termBr).containsExactlyInAnyOrder("mensalidade", "suborno");
    }

    @Test
    void returnsEmptyWhenNoExactMatch() {
        List<GlossaryEntry> result = resolver.resolveExact("inexistente", Direction.PT_TO_BR);

        assertThat(result).isEmpty();
    }

    @Test
    void resolvesPluralViaLema() {
        List<GlossaryEntry> result = resolver.resolveLema("autocarros", Direction.PT_TO_BR);

        assertThat(result).extracting(GlossaryEntry::termPt).containsExactly("autocarro");
    }

    @Test
    void delegatesFuzzyLookupToTheRightColumnByDirection() {
        GlossaryFuzzyLookup fuzzyLookup = mock(GlossaryFuzzyLookup.class);
        GlossaryEntry match = entry("autocarro", "ônibus", false);
        org.mockito.Mockito.when(fuzzyLookup.findSimilarByTermPt("autocaro", GlossaryResolver.FUZZY_THRESHOLD))
                .thenReturn(List.of(match));
        GlossaryResolver resolverWithMockFuzzy = new GlossaryResolver(new GlossaryIndex(), fuzzyLookup);

        List<GlossaryEntry> result = resolverWithMockFuzzy.resolveFuzzy("autocaro", Direction.PT_TO_BR);

        assertThat(result).containsExactly(match);
    }

    private static GlossaryEntry entry(String termPt, String termBr, boolean falseFriend) {
        return new GlossaryEntry(
                termPt, termBr, List.of(), List.of(), "NEUTRO", falseFriend,
                falseFriend ? "nota pt" : null, falseFriend ? "nota br" : null,
                null, null, null, "CURATED");
    }
}
