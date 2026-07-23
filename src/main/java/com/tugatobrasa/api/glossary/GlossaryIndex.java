package com.tugatobrasa.api.glossary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.pt.PortugueseStemmer;
import org.springframework.stereotype.Component;

@Component
public class GlossaryIndex {

    private final AtomicReference<Map<String, List<GlossaryEntry>>> byTermPt =
            new AtomicReference<>(Map.of());
    private final AtomicReference<Map<String, List<GlossaryEntry>>> byTermBr =
            new AtomicReference<>(Map.of());
    private final AtomicReference<Map<String, List<GlossaryEntry>>> byLemaPt =
            new AtomicReference<>(Map.of());
    private final AtomicReference<Map<String, List<GlossaryEntry>>> byLemaBr =
            new AtomicReference<>(Map.of());

    public void reload(List<GlossaryEntry> entries) {
        byTermPt.set(entries.stream().collect(Collectors.groupingBy(GlossaryEntry::termPt)));
        byTermBr.set(entries.stream().collect(Collectors.groupingBy(GlossaryEntry::termBr)));
        byLemaPt.set(entries.stream().collect(Collectors.groupingBy(e -> stem(e.termPt()))));
        byLemaBr.set(entries.stream().collect(Collectors.groupingBy(e -> stem(e.termBr()))));
    }

    public List<GlossaryEntry> lookupByTermPt(String termPt) {
        return byTermPt.get().getOrDefault(termPt, List.of());
    }

    public List<GlossaryEntry> lookupByTermBr(String termBr) {
        return byTermBr.get().getOrDefault(termBr, List.of());
    }

    public List<GlossaryEntry> lookupByLemaPt(String term) {
        return byLemaPt.get().getOrDefault(stem(term), List.of());
    }

    public List<GlossaryEntry> lookupByLemaBr(String term) {
        return byLemaBr.get().getOrDefault(stem(term), List.of());
    }

    public int size() {
        return byTermPt.get().values().stream().mapToInt(List::size).sum();
    }

    private static String stem(String term) {
        // PortugueseStemmer exige buffer maior que o termo (usa espaço extra internamente)
        char[] chars = new char[term.length() + 8];
        term.getChars(0, term.length(), chars, 0);
        int stemmedLength = new PortugueseStemmer().stem(chars, term.length());
        return new String(chars, 0, stemmedLength);
    }
}
