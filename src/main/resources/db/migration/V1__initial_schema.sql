CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE glossary_entry (
    id               BIGSERIAL PRIMARY KEY,
    term_pt          TEXT NOT NULL,
    term_br          TEXT NOT NULL,
    alternatives_pt  TEXT[],
    alternatives_br  TEXT[],
    register         VARCHAR(16) NOT NULL,
    false_friend     BOOLEAN NOT NULL DEFAULT FALSE,
    note_pt          TEXT,
    note_br          TEXT,
    example_pt       TEXT,
    example_br       TEXT,
    region           VARCHAR(32),
    status           VARCHAR(16) NOT NULL,
    CONSTRAINT uq_glossary_entry_term_pair UNIQUE (term_pt, term_br)
);

CREATE INDEX idx_glossary_entry_term_pt_trgm ON glossary_entry USING GIN (term_pt gin_trgm_ops);
CREATE INDEX idx_glossary_entry_term_br_trgm ON glossary_entry USING GIN (term_br gin_trgm_ops);

CREATE TABLE translation_log (
    id           BIGSERIAL PRIMARY KEY,
    input        TEXT NOT NULL,
    output_json  JSONB,
    resolved_by  VARCHAR(16),
    latency_ms   INTEGER,
    feedback     VARCHAR(16),
    suggestion   TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
