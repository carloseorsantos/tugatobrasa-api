package com.tugatobrasa.api.translation;

import java.util.List;

public record TranslationItem(
        String source,
        String target,
        List<String> alternatives,
        String register,
        boolean falseFriend,
        String note,
        String example,
        double confidence,
        String resolvedBy) {
}
