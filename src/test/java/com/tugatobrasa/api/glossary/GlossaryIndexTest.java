package com.tugatobrasa.api.glossary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class GlossaryIndexTest {

    @Test
    void resolvesPluralViaLema() {
        GlossaryIndex index = new GlossaryIndex();
        index.reload(List.of(entry("autocarro", "ônibus")));

        List<GlossaryEntry> result = index.lookupByLemaPt("autocarros");

        assertThat(result).extracting(GlossaryEntry::termPt).containsExactly("autocarro");
    }

    @Test
    void resolvesVerbConjugationViaLema() {
        GlossaryIndex index = new GlossaryIndex();
        index.reload(List.of(entry("desenrascar", "dar um jeito")));

        List<GlossaryEntry> result = index.lookupByLemaPt("desenrascando");

        assertThat(result).extracting(GlossaryEntry::termPt).containsExactly("desenrascar");
    }

    @Test
    void exactTermStillMatchesItsOwnLema() {
        GlossaryIndex index = new GlossaryIndex();
        index.reload(List.of(entry("autocarro", "ônibus")));

        List<GlossaryEntry> result = index.lookupByLemaPt("autocarro");

        assertThat(result).extracting(GlossaryEntry::termPt).containsExactly("autocarro");
    }

    private static GlossaryEntry entry(String termPt, String termBr) {
        return new GlossaryEntry(termPt, termBr, List.of(), List.of(), "NEUTRO", false, null, null, null, null, null, "CURATED");
    }
}
