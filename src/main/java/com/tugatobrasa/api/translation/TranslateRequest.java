package com.tugatobrasa.api.translation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TranslateRequest(
        @NotBlank String text,
        @NotNull Direction direction,
        String context) {
}
