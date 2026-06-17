package com.nhnacademy.springailibrarystudy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class PostgreSqlContainerTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpExtensions() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");
    }

    @Test
    @DisplayName("확장 모듈 설치 확인")
    void extensionsAreAvailable() {
        List<String> extensions = jdbcTemplate.queryForList(
                """
                    SELECT extname
                    FROM pg_extension
                    WHERE extname in ('vector', 'pg_trgm')
                    """,
                    String.class
        );

        assertThat(extensions).contains("vector", "pg_trgm");
    }

    @Test
    @DisplayName("vector 컬럼 생성 및 저장 확인")
    void vectorColumnCanBeCreated() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS vector_smoke_test");
        jdbcTemplate.execute("""
            CREATE TABLE vector_smoke_test (
                id BIGSERIAL PRIMARY KEY,
                embedding vector(3)
            )
            """);

        jdbcTemplate.update("INSERT INTO vector_smoke_test (embedding) VALUES ('[1,2,3]')");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_smoke_test",
                Integer.class
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("pg_trgm similarity 함수 사용 확인")
    void pgTrgmSimilarityCanBeUsed() {
        Double similarity = jdbcTemplate.queryForObject(
                "SELECT similarity('spring cache', 'spring cash')",
                Double.class
        );

        assertThat(similarity)
                .isNotNull()
                .isGreaterThan(0.0);
    }
}
