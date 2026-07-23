package com.tugatobrasa.api.translation;

import java.util.List;

public record TranslateResponse(
        String input,
        List<TranslationItem> translations,
        String fullTranslation,
        List<String> warnings,
        String status,
        List<String> suggestions,
        String contributeUrl) {
}
