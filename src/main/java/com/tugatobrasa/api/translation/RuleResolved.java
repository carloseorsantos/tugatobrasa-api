package com.tugatobrasa.api.translation;

public record RuleResolved(String target, double confidence) implements ResolutionResult {
}
