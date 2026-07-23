package com.tugatobrasa.api.translation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tugatobrasa.api.glossary.GlossaryEntry;

@Service
public class TranslationPipelineService {

    private static final double EXACT_CONFIDENCE = 1.0;
    private static final double LEMA_CONFIDENCE = 0.9;
    private static final double FUZZY_CONFIDENCE = 0.7;

    private final GlossaryResolver glossaryResolver;
    private final NotFoundHandler notFoundHandler;

    public TranslationPipelineService(GlossaryResolver glossaryResolver, NotFoundHandler notFoundHandler) {
        this.glossaryResolver = glossaryResolver;
        this.notFoundHandler = notFoundHandler;
    }

    public ResolutionResult resolve(String rawText, Direction direction) {
        String term = Normalizer.normalize(rawText);

        List<GlossaryEntry> exact = glossaryResolver.resolveExact(term, direction);
        if (!exact.isEmpty()) return new Resolved(exact, EXACT_CONFIDENCE);

        List<GlossaryEntry> lema = glossaryResolver.resolveLema(term, direction);
        if (!lema.isEmpty()) return new Resolved(lema, LEMA_CONFIDENCE);

        List<GlossaryEntry> fuzzy = glossaryResolver.resolveFuzzy(term, direction);
        if (!fuzzy.isEmpty()) return new Resolved(fuzzy, FUZZY_CONFIDENCE);

        return notFoundHandler.handle(rawText, term, direction);
    }
}
