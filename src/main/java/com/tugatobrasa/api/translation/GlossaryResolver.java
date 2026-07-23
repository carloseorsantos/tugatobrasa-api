package com.tugatobrasa.api.translation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.tugatobrasa.api.glossary.GlossaryEntry;
import com.tugatobrasa.api.glossary.GlossaryFuzzyLookup;
import com.tugatobrasa.api.glossary.GlossaryIndex;

// espera termo já normalizado (lowercase/trim/NFC); lista vazia = sem match, próximo resolver assume
@Component
public class GlossaryResolver {

    // ponytail: system design pedia 0.75, mas na prática typo de 1 letra em palavra longa
    // (autocarro/autocaro) já cai pra 0.727 — calibrado empiricamente contra o glossário real
    // (ver refinamento do card). Termos curtos (bué, 3-4 letras) continuam fracos com trigrama;
    // sobe se 0.4 gerar falso positivo, desce mais se continuar perdendo typo real.
    static final double FUZZY_THRESHOLD = 0.4;

    private final GlossaryIndex glossaryIndex;
    private final GlossaryFuzzyLookup fuzzyLookup;

    public GlossaryResolver(GlossaryIndex glossaryIndex, GlossaryFuzzyLookup fuzzyLookup) {
        this.glossaryIndex = glossaryIndex;
        this.fuzzyLookup = fuzzyLookup;
    }

    public List<GlossaryEntry> resolveExact(String term, Direction direction) {
        return direction == Direction.PT_TO_BR
                ? glossaryIndex.lookupByTermPt(term)
                : glossaryIndex.lookupByTermBr(term);
    }

    public List<GlossaryEntry> resolveLema(String term, Direction direction) {
        return direction == Direction.PT_TO_BR
                ? glossaryIndex.lookupByLemaPt(term)
                : glossaryIndex.lookupByLemaBr(term);
    }

    public List<GlossaryEntry> resolveFuzzy(String term, Direction direction) {
        return direction == Direction.PT_TO_BR
                ? fuzzyLookup.findSimilarByTermPt(term, FUZZY_THRESHOLD)
                : fuzzyLookup.findSimilarByTermBr(term, FUZZY_THRESHOLD);
    }
}
