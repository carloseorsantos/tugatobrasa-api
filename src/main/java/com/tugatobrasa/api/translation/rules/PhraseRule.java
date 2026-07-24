package com.tugatobrasa.api.translation.rules;

import java.util.Optional;

import com.tugatobrasa.api.translation.Direction;

public interface PhraseRule {

    Optional<String> apply(String normalizedText, Direction direction);
}
