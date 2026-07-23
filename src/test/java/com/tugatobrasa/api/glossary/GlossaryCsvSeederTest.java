package com.tugatobrasa.api.glossary;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;

@Testcontainers
@SpringBootTest
class GlossaryCsvSeederTest {

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
    private GlossaryCsvSeeder seeder;

    @Autowired
    private GlossaryIndex glossaryIndex;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void seedsGlossaryIntoMemoryAndDatabase() {
        assertThat(glossaryIndex.size()).isGreaterThanOrEqualTo(100);
        assertThat(glossaryIndex.lookupByTermPt("propina")).isNotEmpty();
        assertThat(glossaryIndex.lookupByTermBr("legal")).isNotEmpty();

        long falseFriendCount = glossaryIndex.lookupByTermPt("propina").stream()
                .filter(GlossaryEntry::falseFriend)
                .count();
        assertThat(falseFriendCount).isGreaterThan(0);
    }

    @Test
    void seedIsIdempotent() {
        Integer countBefore = jdbcTemplate.queryForObject("SELECT count(*) FROM glossary_entry", Integer.class);

        seeder.seed();
        seeder.seed();

        Integer countAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM glossary_entry", Integer.class);
        assertThat(countAfter).isEqualTo(countBefore);
    }

    @Test
    void glossaryHasFalseFriendEntries() {
        Integer falseFriendCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM glossary_entry WHERE false_friend = true", Integer.class);
        assertThat(falseFriendCount).isGreaterThan(0);
    }

    @Test
    void pgTrgmResolvesFuzzyTypos() {
        List<String> matches = jdbcTemplate.queryForList(
                "SELECT term_pt FROM glossary_entry WHERE similarity(term_pt, ?) > 0.3 ORDER BY similarity(term_pt, ?) DESC",
                String.class, "autocaro", "autocaro");

        assertThat(matches).contains("autocarro");
    }

    @Test
    void linguisticCasesMatchTheSeededGlossary() throws IOException {
        List<CSVRecord> cases = readLinguisticCases();
        assertThat(cases).isNotEmpty();

        for (CSVRecord row : cases) {
            String input = row.get("input");
            String direction = row.get("direction");
            String expectedTerm = row.get("expected_term");
            boolean expectAlert = Boolean.parseBoolean(row.get("expect_false_friend_alert"));

            List<GlossaryEntry> candidates = "PT_TO_BR".equals(direction)
                    ? glossaryIndex.lookupByTermPt(input)
                    : glossaryIndex.lookupByTermBr(input);

            boolean matched = candidates.stream().anyMatch(e -> {
                String target = "PT_TO_BR".equals(direction) ? e.termBr() : e.termPt();
                return target.equals(expectedTerm) && e.falseFriend() == expectAlert;
            });

            assertThat(matched)
                    .as("caso linguístico: %s (%s) -> %s, alerta=%s", input, direction, expectedTerm, expectAlert)
                    .isTrue();
        }
    }

    private static List<CSVRecord> readLinguisticCases() throws IOException {
        try (InputStream is = GlossaryCsvSeederTest.class.getClassLoader()
                .getResourceAsStream("linguistic-cases.csv")) {
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            try (CSVParser parser = format.parse(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return parser.getRecords();
            }
        }
    }
}
