package com.nhnacademy.springailibrarystudy.cache.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookSearchCacheQueryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final BookSearchCacheRowMapper rowMapper;   // RowMapper 주입받아 사용

    private static final String EMBEDDING_MODEL = "bge-m3";

    /**
     * 가장 비슷한 캐시 1개를 조회한다.
     * 만료 안 됐고(expires_at > now), 유사도가 threshold 이상인 것 중 가장 비슷한 것.
     */
    public Optional<CachedResult> findMostSimilar(float[] queryVector, double threshold) {
        String vectorString = arrayToVectorString(queryVector);

        List<CachedResult> results = jdbcTemplate.query(
                """
                SELECT
                    id,
                    result,
                    1 - (embedding <=> ?::vector) AS similarity
                FROM book_search_cache
                WHERE embedding_model = ?
                  AND expires_at > ?
                  AND 1 - (embedding <=> ?::vector) >= ?
                ORDER BY embedding <=> ?::vector
                LIMIT 1
                """,
                rowMapper,   // 람다 대신 주입받은 RowMapper 사용
                vectorString, EMBEDDING_MODEL, LocalDateTime.now(), vectorString, threshold, vectorString
        );

        return results.stream().findFirst();
    }

    /** 캐시 적중 시 마지막 접근 시각/횟수 갱신 */
    public void touch(Long cacheId) {
        jdbcTemplate.update(
                "UPDATE book_search_cache SET last_accessed_at = ?, access_count = access_count + 1 WHERE id = ?",
                LocalDateTime.now(), cacheId
        );
    }

    /** 새 캐시 저장 */
    public void save(String query, float[] embedding, String result, int ttlSeconds) {
        String vectorString = arrayToVectorString(embedding);
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(
                """
                INSERT INTO book_search_cache
                    (query, embedding, embedding_model, result, created_at, expires_at, last_accessed_at, access_count)
                VALUES (?, ?::vector, ?, ?, ?, ?, ?, 0)
                """,
                query, vectorString, EMBEDDING_MODEL, result, now, now.plusSeconds(ttlSeconds), now
        );
    }

    /** 만료된 캐시 삭제, 삭제된 개수 반환 */
    public int deleteExpired() {
        return jdbcTemplate.update(
                "DELETE FROM book_search_cache WHERE expires_at < ?",
                LocalDateTime.now()
        );
    }

    /** BookQueryRepository.arrayToVectorString() 과 동일한 변환 로직 */
    private String arrayToVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}