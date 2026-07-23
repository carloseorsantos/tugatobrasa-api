package com.tugatobrasa.api.translation;

import java.util.List;

public record NotResolved(List<String> suggestions, String contributeUrl) implements ResolutionResult {
}
