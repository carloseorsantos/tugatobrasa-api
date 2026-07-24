package com.tugatobrasa.api.translation.rules;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.tugatobrasa.api.translation.Direction;

@Component
public class RuleResolver {

    private final List<PhraseRule> rules = List.of(new GerundEstarARule());

    public Optional<String> resolve(String normalizedText, Direction direction) {
        for (PhraseRule rule : rules) {
            Optional<String> result = rule.apply(normalizedText, direction);
            if (result.isPresent()) return result;
        }
        return Optional.empty();
    }
}
