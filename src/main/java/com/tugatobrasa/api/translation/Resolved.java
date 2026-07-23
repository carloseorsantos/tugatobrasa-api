package com.tugatobrasa.api.translation;

import java.util.List;

import com.tugatobrasa.api.glossary.GlossaryEntry;

public record Resolved(List<GlossaryEntry> entries) implements ResolutionResult {
}
