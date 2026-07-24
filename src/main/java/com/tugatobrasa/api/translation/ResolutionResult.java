package com.tugatobrasa.api.translation;

public sealed interface ResolutionResult permits Resolved, NotResolved, RuleResolved {
}
