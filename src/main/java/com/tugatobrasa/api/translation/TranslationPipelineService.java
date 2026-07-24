package com.tugatobrasa.api.translation;

import java.util.List;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tugatobrasa.api.glossary.GlossaryEntry;
import com.tugatobrasa.api.translation.rules.RuleResolver;

@Service
public class TranslationPipelineService {

    private static final double EXACT_CONFIDENCE = 1.0;
    private static final double LEMA_CONFIDENCE = 0.9;
    private static final double FUZZY_CONFIDENCE = 0.7;
    private static final double RULE_CONFIDENCE = 1.0;

    private final GlossaryResolver glossaryResolver;
    private final RuleResolver ruleResolver;
    private final NotFoundHandler notFoundHandler;

    public TranslationPipelineService(
            GlossaryResolver glossaryResolver, RuleResolver ruleResolver, NotFoundHandler notFoundHandler) {
        this.glossaryResolver = glossaryResolver;
        this.ruleResolver = ruleResolver;
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

        Optional<String> rule = ruleResolver.resolve(term, direction);
        if (rule.isPresent()) return new RuleResolved(rule.get(), RULE_CONFIDENCE);

        return notFoundHandler.handle(rawText, term, direction);
    }
}
