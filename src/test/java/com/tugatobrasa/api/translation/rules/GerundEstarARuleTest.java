package com.tugatobrasa.api.translation.rules;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.tugatobrasa.api.translation.Direction;

class GerundEstarARuleTest {

    private final GerundEstarARule rule = new GerundEstarARule();

    @Test
    void convertsRegularArVerbPtToBr() {
        assertThat(rule.apply("estou a falar", Direction.PT_TO_BR)).contains("estou falando");
    }

    @Test
    void convertsRegularErVerbBrToPt() {
        assertThat(rule.apply("estou comendo", Direction.BR_TO_PT)).contains("estou a comer");
    }

    @Test
    void convertsRegularIrVerbBothDirections() {
        assertThat(rule.apply("estás a partir", Direction.PT_TO_BR)).contains("estás partindo");
        assertThat(rule.apply("estás partindo", Direction.BR_TO_PT)).contains("estás a partir");
    }

    @Test
    void convertsPorFamilyVerbBothDirections() {
        assertThat(rule.apply("está a pôr a mesa", Direction.PT_TO_BR)).contains("está pondo a mesa");
        assertThat(rule.apply("está pondo a mesa", Direction.BR_TO_PT)).contains("está a pôr a mesa");
        assertThat(rule.apply("estamos a compor uma música", Direction.PT_TO_BR)).contains("estamos compondo uma música");
    }

    @Test
    void keepsSurroundingTextIntact() {
        assertThat(rule.apply("ele disse que estou a trabalhar hoje", Direction.PT_TO_BR))
                .contains("ele disse que estou trabalhando hoje");
    }

    @Test
    void doesNotMatchWhenNoEstarConstruction() {
        assertThat(rule.apply("gosto de bacalhau", Direction.PT_TO_BR)).isEmpty();
    }

    @Test
    void doesNotGuessWhenWordAfterEstarAIsNotAVerb() {
        assertThat(rule.apply("estou a caminho", Direction.PT_TO_BR)).isEmpty();
    }

    @Test
    void doesNotMatchEstarAloneWithoutInfinitiveOrGerund() {
        assertThat(rule.apply("estou cansado", Direction.BR_TO_PT)).isEmpty();
    }
}
