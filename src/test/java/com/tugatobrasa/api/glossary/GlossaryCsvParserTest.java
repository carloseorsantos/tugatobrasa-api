package com.tugatobrasa.api.glossary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GlossaryCsvParserTest {

    private final GlossaryCsvParser parser = new GlossaryCsvParser();

    private static final String HEADER =
            "term_pt,term_br,alternatives_pt,alternatives_br,register,false_friend,note_pt,note_br,example_pt,example_br,region,status";

    @TempDir
    Path tempDir;

    @Test
    void failsWithLineAndFieldWhenRegisterIsInvalid() throws IOException {
        Path csv = writeCsv(HEADER, "termo,termo_br,,,REGISTRO_INEXISTENTE,false,,,,,,CURATED");

        assertThatThrownBy(() -> parser.parse(csv))
                .isInstanceOf(GlossaryCsvException.class)
                .hasMessageContaining("linha 2")
                .hasMessageContaining("register inválido");
    }

    @Test
    void failsWhenFalseFriendMissingNotes() throws IOException {
        Path csv = writeCsv(HEADER, "termo,termo_br,,,NEUTRO,true,,,,,,CURATED");

        assertThatThrownBy(() -> parser.parse(csv))
                .isInstanceOf(GlossaryCsvException.class)
                .hasMessageContaining("linha 2")
                .hasMessageContaining("false_friend=true exige note_pt e note_br");
    }

    @Test
    void failsWhenGiriaMissingExamples() throws IOException {
        Path csv = writeCsv(HEADER, "termo,termo_br,,,GIRIA,false,,,,,,CURATED");

        assertThatThrownBy(() -> parser.parse(csv))
                .isInstanceOf(GlossaryCsvException.class)
                .hasMessageContaining("register GIRIA exige example_pt e example_br");
    }

    @Test
    void failsOnDuplicatePair() throws IOException {
        Path csv = writeCsv(HEADER,
                "termo,termo_br,,,NEUTRO,false,,,,,,CURATED",
                "termo,termo_br,,,NEUTRO,false,,,,,,CURATED");

        assertThatThrownBy(() -> parser.parse(csv))
                .isInstanceOf(GlossaryCsvException.class)
                .hasMessageContaining("linha 3")
                .hasMessageContaining("par duplicado");
    }

    @Test
    void parsesValidRowsIntoEntries() throws IOException {
        Path csv = writeCsv(HEADER,
                "autocarro,ônibus,machimbombo,busão|ônibus rural,NEUTRO,false,,,,,,CURATED");

        var entries = parser.parse(csv);

        assertThat(entries).hasSize(1);
        GlossaryEntry e = entries.get(0);
        assertThat(e.termPt()).isEqualTo("autocarro");
        assertThat(e.termBr()).isEqualTo("ônibus");
        assertThat(e.alternativesPt()).containsExactly("machimbombo");
        assertThat(e.alternativesBr()).containsExactly("busão", "ônibus rural");
        assertThat(e.falseFriend()).isFalse();
    }

    private Path writeCsv(String... lines) throws IOException {
        Path csv = tempDir.resolve("glossario.csv");
        Files.writeString(csv, String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
        return csv;
    }
}
