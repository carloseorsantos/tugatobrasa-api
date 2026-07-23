package com.tugatobrasa.api.glossary;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

// reutilizável: usado tanto pelo GlossaryResolver (fallback fuzzy) quanto pelo NotFoundHandler (sugestões)
@Component
public class GlossaryFuzzyLookup {

    private static final int MAX_RESULTS = 5;

    private final JdbcTemplate jdbcTemplate;

    public GlossaryFuzzyLookup(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<GlossaryEntry> findSimilarByTermPt(String term, double threshold) {
        return findSimilar("term_pt", term, threshold);
    }

    public List<GlossaryEntry> findSimilarByTermBr(String term, double threshold) {
        return findSimilar("term_br", term, threshold);
    }

    private List<GlossaryEntry> findSimilar(String column, String term, double threshold) {
        String sql = """
                SELECT term_pt, term_br, alternatives_pt, alternatives_br, register, false_friend,
                       note_pt, note_br, example_pt, example_br, region, status
                FROM glossary_entry
                WHERE status = 'CURATED' AND similarity(%1$s, ?) > ?
                ORDER BY similarity(%1$s, ?) DESC
                LIMIT %2$d
                """.formatted(column, MAX_RESULTS);
        return jdbcTemplate.query(sql, ROW_MAPPER, term, threshold, term);
    }

    private static final RowMapper<GlossaryEntry> ROW_MAPPER = (ResultSet rs, int rowNum) -> new GlossaryEntry(
            rs.getString("term_pt"),
            rs.getString("term_br"),
            toList(rs.getArray("alternatives_pt")),
            toList(rs.getArray("alternatives_br")),
            rs.getString("register"),
            rs.getBoolean("false_friend"),
            rs.getString("note_pt"),
            rs.getString("note_br"),
            rs.getString("example_pt"),
            rs.getString("example_br"),
            rs.getString("region"),
            rs.getString("status"));

    private static List<String> toList(Array sqlArray) throws SQLException {
        if (sqlArray == null) return List.of();
        return List.of((String[]) sqlArray.getArray());
    }
}
