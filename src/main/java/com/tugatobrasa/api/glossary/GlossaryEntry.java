package com.tugatobrasa.api.glossary;

import java.util.List;

public record GlossaryEntry(
        String termPt,
        String termBr,
        List<String> alternativesPt,
        List<String> alternativesBr,
        String register,
        boolean falseFriend,
        String notePt,
        String noteBr,
        String examplePt,
        String exampleBr,
        String region,
        String status) {
}
