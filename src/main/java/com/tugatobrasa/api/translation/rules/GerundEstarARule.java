package com.tugatobrasa.api.translation.rules;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tugatobrasa.api.translation.Direction;

/**
 * estar + a + infinitivo (pt-PT) ⇌ estar + gerúndio (pt-BR). Ver ADR-0005.
 *
 * Gerúndio em português é quase totalmente regular por sufixo (-ar/-er/-ir →
 * -ando/-endo/-indo) — cobre até verbos como ser/ir/vir/dizer sem tabela. A
 * exceção real é a família de "pôr" (não termina em -ar/-er/-ir), tratada à
 * parte. Verbo fora da regra regular e fora da tabela não casa — nunca chuta
 * uma forma.
 */
public final class GerundEstarARule implements PhraseRule {

    private static final String ESTAR = "estou|estás|está|estamos|estão";
    private static final String LETTERS = "[a-zàáâãçéêíóôõú]+";

    private static final Pattern PT_TO_BR_PATTERN =
            Pattern.compile("\\b(" + ESTAR + ")\\s+a\\s+(" + LETTERS + ")\\b");
    private static final Pattern BR_TO_PT_PATTERN =
            Pattern.compile("\\b(" + ESTAR + ")\\s+(" + LETTERS + ")\\b");

    private static final Map<String, String> POR_FAMILY_TO_GERUND = Map.ofEntries(
            Map.entry("pôr", "pondo"), Map.entry("compor", "compondo"), Map.entry("propor", "propondo"),
            Map.entry("dispor", "dispondo"), Map.entry("supor", "supondo"), Map.entry("opor", "opondo"),
            Map.entry("repor", "repondo"), Map.entry("expor", "expondo"), Map.entry("impor", "impondo"));

    private static final Map<String, String> POR_FAMILY_TO_INFINITIVE =
            POR_FAMILY_TO_GERUND.entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    @Override
    public Optional<String> apply(String normalizedText, Direction direction) {
        return direction == Direction.PT_TO_BR
                ? applyPtToBr(normalizedText)
                : applyBrToPt(normalizedText);
    }

    private Optional<String> applyPtToBr(String text) {
        Matcher m = PT_TO_BR_PATTERN.matcher(text);
        if (!m.find()) return Optional.empty();

        String estarForm = m.group(1);
        String infinitive = m.group(2);
        Optional<String> gerund = toGerund(infinitive);
        if (gerund.isEmpty()) return Optional.empty();

        return Optional.of(text.substring(0, m.start())
                + estarForm + " " + gerund.get()
                + text.substring(m.end()));
    }

    private Optional<String> applyBrToPt(String text) {
        Matcher m = BR_TO_PT_PATTERN.matcher(text);
        if (!m.find()) return Optional.empty();

        String estarForm = m.group(1);
        String gerund = m.group(2);
        Optional<String> infinitive = toInfinitive(gerund);
        if (infinitive.isEmpty()) return Optional.empty();

        return Optional.of(text.substring(0, m.start())
                + estarForm + " a " + infinitive.get()
                + text.substring(m.end()));
    }

    private static Optional<String> toGerund(String infinitive) {
        if (POR_FAMILY_TO_GERUND.containsKey(infinitive)) {
            return Optional.of(POR_FAMILY_TO_GERUND.get(infinitive));
        }
        if (infinitive.endsWith("ar")) return Optional.of(stripLast(infinitive, 2) + "ando");
        if (infinitive.endsWith("er")) return Optional.of(stripLast(infinitive, 2) + "endo");
        if (infinitive.endsWith("ir")) return Optional.of(stripLast(infinitive, 2) + "indo");
        return Optional.empty();
    }

    private static Optional<String> toInfinitive(String gerund) {
        if (POR_FAMILY_TO_INFINITIVE.containsKey(gerund)) {
            return Optional.of(POR_FAMILY_TO_INFINITIVE.get(gerund));
        }
        if (gerund.endsWith("ando")) return Optional.of(stripLast(gerund, 4) + "ar");
        if (gerund.endsWith("endo")) return Optional.of(stripLast(gerund, 4) + "er");
        if (gerund.endsWith("indo")) return Optional.of(stripLast(gerund, 4) + "ir");
        return Optional.empty();
    }

    private static String stripLast(String s, int n) {
        return s.substring(0, s.length() - n);
    }
}
