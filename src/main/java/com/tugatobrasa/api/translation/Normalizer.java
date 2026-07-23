package com.tugatobrasa.api.translation;

import java.util.Locale;

final class Normalizer {

    private Normalizer() {
    }

    static String normalize(String input) {
        if (input == null) return "";
        return java.text.Normalizer.normalize(input.trim().toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFC);
    }
}
