package com.tugatobrasa.api.translation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tugatobrasa.api.glossary.GlossaryEntry;

@Service
public class TranslationPipelineService {

    private final GlossaryResolver glossaryResolver;

    public TranslationPipelineService(GlossaryResolver glossaryResolver) {
        this.glossaryResolver = glossaryResolver;
    }

    public ResolutionResult resolve(String rawText, Direction direction) {
        String term = Normalizer.normalize(rawText);
        List<GlossaryEntry> matches = glossaryResolver.resolveExact(term, direction);
        return matches.isEmpty() ? new NotResolved() : new Resolved(matches);
    }
}
