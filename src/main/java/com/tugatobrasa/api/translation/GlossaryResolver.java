package com.tugatobrasa.api.translation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.tugatobrasa.api.glossary.GlossaryEntry;
import com.tugatobrasa.api.glossary.GlossaryIndex;

// espera termo já normalizado (lowercase/trim/NFC); lista vazia = sem match, próximo resolver assume
@Component
public class GlossaryResolver {

    private final GlossaryIndex glossaryIndex;

    public GlossaryResolver(GlossaryIndex glossaryIndex) {
        this.glossaryIndex = glossaryIndex;
    }

    public List<GlossaryEntry> resolveExact(String term, Direction direction) {
        return direction == Direction.PT_TO_BR
                ? glossaryIndex.lookupByTermPt(term)
                : glossaryIndex.lookupByTermBr(term);
    }
}
