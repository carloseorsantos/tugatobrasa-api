package com.tugatobrasa.api.glossary;

import static org.assertj.core.api.Assertions.assertThat;

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
}
