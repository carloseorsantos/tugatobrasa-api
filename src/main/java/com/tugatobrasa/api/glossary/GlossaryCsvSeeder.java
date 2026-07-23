package com.tugatobrasa.api.glossary;

import java.nio.file.Path;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class GlossaryCsvSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GlossaryCsvSeeder.class);

    private static final String UPSERT_SQL = """
            INSERT INTO glossary_entry
                (term_pt, term_br, alternatives_pt, alternatives_br, register, false_friend,
                 note_pt, note_br, example_pt, example_br, region, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (term_pt, term_br) DO UPDATE SET
                alternatives_pt = EXCLUDED.alternatives_pt,
                alternatives_br = EXCLUDED.alternatives_br,
                register        = EXCLUDED.register,
                false_friend    = EXCLUDED.false_friend,
                note_pt         = EXCLUDED.note_pt,
                note_br         = EXCLUDED.note_br,
                example_pt      = EXCLUDED.example_pt,
                example_br      = EXCLUDED.example_br,
                region          = EXCLUDED.region,
                status          = EXCLUDED.status
            """;

    private final JdbcTemplate jdbcTemplate;
    private final GlossaryIndex glossaryIndex;
    private final GlossaryCsvParser parser = new GlossaryCsvParser();
    private final Path csvPath;

    public GlossaryCsvSeeder(
            JdbcTemplate jdbcTemplate,
            GlossaryIndex glossaryIndex,
            @Value("${app.glossary.csv-path}") String csvPath) {
        this.jdbcTemplate = jdbcTemplate;
        this.glossaryIndex = glossaryIndex;
        this.csvPath = Path.of(csvPath);
    }

    @Override
    public void run(ApplicationArguments args) {
        seed();
    }

    public void seed() {
        List<GlossaryEntry> entries = parser.parse(csvPath);
        upsert(entries);

        List<GlossaryEntry> curated = entries.stream()
                .filter(e -> "CURATED".equals(e.status()))
                .toList();
        glossaryIndex.reload(curated);

        log.info("Glossário carregado: {} linhas no CSV, {} CURATED em memória", entries.size(), curated.size());
    }

    private void upsert(List<GlossaryEntry> entries) {
        jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                GlossaryEntry e = entries.get(i);
                Array altPt = toSqlArray(ps, e.alternativesPt());
                Array altBr = toSqlArray(ps, e.alternativesBr());
                ps.setString(1, e.termPt());
                ps.setString(2, e.termBr());
                ps.setArray(3, altPt);
                ps.setArray(4, altBr);
                ps.setString(5, e.register());
                ps.setBoolean(6, e.falseFriend());
                ps.setString(7, e.notePt());
                ps.setString(8, e.noteBr());
                ps.setString(9, e.examplePt());
                ps.setString(10, e.exampleBr());
                ps.setString(11, e.region());
                ps.setString(12, e.status());
            }

            @Override
            public int getBatchSize() {
                return entries.size();
            }
        });
    }

    private static Array toSqlArray(PreparedStatement ps, List<String> values) throws SQLException {
        return ps.getConnection().createArrayOf("text", values.toArray());
    }
}
