package com.tugatobrasa.api.glossary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class GlossaryIndex {

    private final AtomicReference<Map<String, List<GlossaryEntry>>> byTermPt =
            new AtomicReference<>(Map.of());
    private final AtomicReference<Map<String, List<GlossaryEntry>>> byTermBr =
            new AtomicReference<>(Map.of());

    void reload(List<GlossaryEntry> entries) {
        byTermPt.set(entries.stream().collect(Collectors.groupingBy(GlossaryEntry::termPt)));
        byTermBr.set(entries.stream().collect(Collectors.groupingBy(GlossaryEntry::termBr)));
    }

    public List<GlossaryEntry> lookupByTermPt(String termPt) {
        return byTermPt.get().getOrDefault(termPt, List.of());
    }

    public List<GlossaryEntry> lookupByTermBr(String termBr) {
        return byTermBr.get().getOrDefault(termBr, List.of());
    }

    public int size() {
        return byTermPt.get().values().stream().mapToInt(List::size).sum();
    }
}
