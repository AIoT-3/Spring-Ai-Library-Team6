package com.nhnacademy.springailibrarystudy.book.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Slf4j
@Tag("analysis")
@EnabledIfSystemProperty(named = "search.index.explain", matches = "true")
class BookSearchIndexExplainTest {

    private static final int EXPECTED_ROW_COUNT = 157_118;
    private static final String DEFAULT_JDBC_URL =
            "jdbc:postgresql://localhost:5432/library?reWriteBatchedInserts=true";
    private static final String DEFAULT_USERNAME = "library";
    private static final String DEFAULT_PASSWORD = "library";

    // spring context 없이 jdbcTemplate를 직접 생성하여 사용
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(new DriverManagerDataSource(
            System.getProperty("search.index.jdbc-url", DEFAULT_JDBC_URL),
            System.getProperty("search.index.username", DEFAULT_USERNAME),
            System.getProperty("search.index.password", DEFAULT_PASSWORD)
    ));

    @Test
    void compareSearchIndexPlans() {
        log.info("book row count 확인 시작");
        assertEquals(EXPECTED_ROW_COUNT, bookCount());
        log.info("book row count 확인 완료: {}", EXPECTED_ROW_COUNT);

        try {
            resetCandidateIndexes();
            explainAll("baseline");

            resetCandidateIndexes();
            createKdcBtreeIndex();
            explainAll("kdc-btree");

            resetCandidateIndexes();
            createKdcPatternIndex();
            explainAll("kdc-pattern");

            resetCandidateIndexes();
            createKeywordTrgmIndexes();
            explainAll("keyword-trgm");

            resetCandidateIndexes();
            createDescriptionFullTextSearchIndex();
            explainAll("description-fts");

            resetCandidateIndexes();
            createKdcPatternIndex();
            createKeywordTrgmIndexes();
            createDescriptionFullTextSearchIndex();
            explainAll("combined-candidate");
        } finally {
            resetCandidateIndexes();
        }
    }

    private int bookCount() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from books", Integer.class);
        return count == null ? 0 : count;
    }

    private void explainAll(String indexSet) {
        log.info("{} analyze 시작", indexSet);
        analyzeBooks();
        log.info("\n==================== {} ====================", indexSet);

        for (SearchScenario scenario : searchScenarios()) {
            log.info("{} / {} explain 시작", indexSet, scenario.name());
            log.info("\n--- {} / {} ---\n{}", indexSet, scenario.name(), explain(scenario.sql()));
        }
    }

    private String explain(String sql) {
        // 쿼리 실행 계획 및 실제 실행 시간 및 버퍼 사용량까지 확인하기 위해 explain (analyze, buffers) 사용
        List<String> rows = jdbcTemplate.queryForList("""
                explain (analyze, buffers)
                %s
                """.formatted(sql), String.class);

        return String.join("\n", rows);
    }

    private List<SearchScenario> searchScenarios() {
        return List.of(
                new SearchScenario("isbn-exact-content", """
                        select id, volume_title, title, author_name, publisher_name, published_date, price, image_url
                        from books
                        where isbn13 = '9788968481475'
                        order by id
                        limit 20
                        """),
                new SearchScenario("isbn-exact-count", """
                        select count(*)
                        from books
                        where isbn13 = '9788968481475'
                        """),
                new SearchScenario("kdc-prefix-content", """
                        select id, volume_title, title, author_name, publisher_name, published_date, price, image_url
                        from books
                        where kdc_code like '500%'
                        order by id
                        limit 20
                        """),
                new SearchScenario("kdc-prefix-count", """
                        select count(*)
                        from books
                        where kdc_code like '500%'
                        """),
                new SearchScenario("keyword-content", """
                        select id, volume_title, title, author_name, publisher_name, published_date, price, image_url
                        from books
                        where lower(title) like '%자바%'
                           or lower(author_name) like '%자바%'
                           or lower(publisher_name) like '%자바%'
                        order by id
                        limit 20
                        """),
                new SearchScenario("keyword-count", """
                        select count(*)
                        from books
                        where lower(title) like '%자바%'
                           or lower(author_name) like '%자바%'
                           or lower(publisher_name) like '%자바%'
                        """),
                new SearchScenario("keyword-kdc-content", """
                        select id, volume_title, title, author_name, publisher_name, published_date, price, image_url
                        from books
                        where (
                            lower(title) like '%자바%'
                            or lower(author_name) like '%자바%'
                            or lower(publisher_name) like '%자바%'
                        )
                        and kdc_code like '500%'
                        order by id
                        limit 20
                        """),
                new SearchScenario("keyword-kdc-count", """
                        select count(*)
                        from books
                        where (
                            lower(title) like '%자바%'
                            or lower(author_name) like '%자바%'
                            or lower(publisher_name) like '%자바%'
                        )
                        and kdc_code like '500%'
                        """),
                new SearchScenario("description-fts-content", """
                        select id, volume_title, title, author_name, publisher_name, published_date, price, image_url
                        from books
                        where to_tsvector('simple', coalesce(description, ''))
                              @@ plainto_tsquery('simple', '자바 프로그래밍')
                        order by id
                        limit 20
                        """),
                new SearchScenario("description-fts-count", """
                        select count(*)
                        from books
                        where to_tsvector('simple', coalesce(description, ''))
                              @@ plainto_tsquery('simple', '자바 프로그래밍')
                        """)
        );
    }

    private void resetCandidateIndexes() {
        log.info("후보 인덱스 제거 시작");
        jdbcTemplate.execute("drop index if exists idx_books_kdc_code");
        jdbcTemplate.execute("drop index if exists idx_books_kdc_code_pattern");
        jdbcTemplate.execute("drop index if exists idx_books_title_trgm");
        jdbcTemplate.execute("drop index if exists idx_books_author_name_trgm");
        jdbcTemplate.execute("drop index if exists idx_books_publisher_name_trgm");
        jdbcTemplate.execute("drop index if exists idx_books_description_fts");
        log.info("후보 인덱스 제거 완료");
    }

    private void createKdcBtreeIndex() {
        log.info("idx_books_kdc_code 생성 시작");
        jdbcTemplate.execute("create index idx_books_kdc_code on books (kdc_code)");
        log.info("idx_books_kdc_code 생성 완료");
    }

    private void createKdcPatternIndex() {
        log.info("idx_books_kdc_code_pattern 생성 시작");
        jdbcTemplate.execute("create index idx_books_kdc_code_pattern on books (kdc_code varchar_pattern_ops)");
        log.info("idx_books_kdc_code_pattern 생성 완료");
    }

    private void createKeywordTrgmIndexes() {
        log.info("pg_trgm extension 확인 시작");
        jdbcTemplate.execute("create extension if not exists pg_trgm");
        log.info("pg_trgm extension 확인 완료");

        log.info("idx_books_title_trgm 생성 시작");
        jdbcTemplate.execute("create index idx_books_title_trgm on books using gin (lower(title) gin_trgm_ops)");
        log.info("idx_books_title_trgm 생성 완료");

        log.info("idx_books_author_name_trgm 생성 시작");
        jdbcTemplate.execute("create index idx_books_author_name_trgm on books using gin (lower(author_name) gin_trgm_ops)");
        log.info("idx_books_author_name_trgm 생성 완료");

        log.info("idx_books_publisher_name_trgm 생성 시작");
        jdbcTemplate.execute("create index idx_books_publisher_name_trgm on books using gin (lower(publisher_name) gin_trgm_ops)");
        log.info("idx_books_publisher_name_trgm 생성 완료");
    }

    private void createDescriptionFullTextSearchIndex() {
        log.info("idx_books_description_fts 생성 시작");
        jdbcTemplate.execute("""
                create index idx_books_description_fts
                on books using gin (to_tsvector('simple', coalesce(description, '')))
                """);
        log.info("idx_books_description_fts 생성 완료");
    }

    private void analyzeBooks() {
        // 쿼리 계획을 세울 때 통계를 최신화해야 하므로 analyze 수행
        jdbcTemplate.execute("analyze books");
    }

    private record SearchScenario(String name, String sql) {
    }
}
