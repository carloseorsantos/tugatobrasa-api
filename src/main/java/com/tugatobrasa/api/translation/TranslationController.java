package com.tugatobrasa.api.translation;

import java.util.List;
import java.util.Objects;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tugatobrasa.api.glossary.GlossaryEntry;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class TranslationController {

    private final TranslationPipelineService pipelineService;

    public TranslationController(TranslationPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/translate")
    public TranslateResponse translate(@Valid @RequestBody TranslateRequest request) {
        ResolutionResult result = pipelineService.resolve(request.text(), request.direction());
        return switch (result) {
            case Resolved r -> toResponse(request, r.entries(), r.confidence());
            case NotResolved ignored -> new TranslateResponse(request.text(), List.of(), null, List.of(), "NOT_FOUND");
        };
    }

    private TranslateResponse toResponse(TranslateRequest request, List<GlossaryEntry> entries, double confidence) {
        List<TranslationItem> items = entries.stream()
                .map(e -> toItem(e, request.direction(), confidence))
                .toList();

        List<String> warnings = items.stream()
                .filter(TranslationItem::falseFriend)
                .map(TranslationItem::note)
                .filter(Objects::nonNull)
                .toList();

        String fullTranslation = items.size() == 1 ? items.get(0).target() : null;

        return new TranslateResponse(request.text(), items, fullTranslation, warnings, null);
    }

    private TranslationItem toItem(GlossaryEntry entry, Direction direction, double confidence) {
        boolean ptToBr = direction == Direction.PT_TO_BR;
        return new TranslationItem(
                ptToBr ? entry.termPt() : entry.termBr(),
                ptToBr ? entry.termBr() : entry.termPt(),
                ptToBr ? entry.alternativesBr() : entry.alternativesPt(),
                entry.register(),
                entry.falseFriend(),
                ptToBr ? entry.noteBr() : entry.notePt(),
                ptToBr ? entry.exampleBr() : entry.examplePt(),
                confidence,
                "GLOSSARY");
    }
}
