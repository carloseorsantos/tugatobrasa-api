package com.tugatobrasa.api.translation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// caso ponta a ponta de todo linguistic-cases.csv via HTTP real (o teste da Fase 1 cobre só o índice em memória)
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TranslateLinguisticCasesTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("tugatobrasa")
            .withUsername("tugatobrasa")
            .withPassword("tugatobrasa");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "{0} ({1}) -> {2}, alerta={3}")
    @MethodSource("linguisticCases")
    void resolvesEachLinguisticCaseViaRealEndpoint(
            String input, String direction, String expectedTerm, boolean expectAlert) throws Exception {
        var result = mockMvc.perform(post("/api/v1/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"%s\",\"direction\":\"%s\"}".formatted(input, direction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullTranslation").value(expectedTerm));

        if (expectAlert) {
            result.andExpect(jsonPath("$.warnings[0]").isNotEmpty());
        }
    }

    @Test
    void unknownTermReturnsSuggestionsAndContributeUrl() throws Exception {
        mockMvc.perform(post("/api/v1/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"termocompletamentedesconhecido","direction":"PT_TO_BR"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.translations").isEmpty())
                .andExpect(jsonPath("$.contributeUrl").value(
                        "https://github.com/carloseorsantos/tugatobrasa-api/issues/new?template=novo-termo.yml&termo=termocompletamentedesconhecido"));
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> linguisticCases() throws IOException {
        try (InputStream is = TranslateLinguisticCasesTest.class.getClassLoader()
                .getResourceAsStream("linguistic-cases.csv")) {
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            try (CSVParser parser = format.parse(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                List<CSVRecord> records = parser.getRecords();
                return records.stream().map(r -> org.junit.jupiter.params.provider.Arguments.of(
                        r.get("input"), r.get("direction"), r.get("expected_term"),
                        Boolean.parseBoolean(r.get("expect_false_friend_alert"))));
            }
        }
    }
}
