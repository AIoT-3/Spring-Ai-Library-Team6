-- 2026-06-25 EXPLAIN 결과에 따른 인덱스

CREATE INDEX IF NOT EXISTS idx_books_kdc_code_pattern
    ON books (kdc_code varchar_pattern_ops);

CREATE INDEX IF NOT EXISTS idx_books_description_fts
    ON books USING gin (to_tsvector('simple', coalesce(description, '')));
