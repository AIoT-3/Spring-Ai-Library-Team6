package com.nhnacademy.springailibrarystudy.personalization.infrastructure;

import com.nhnacademy.springailibrarystudy.personalization.domain.CandidatePreferenceSimilarity;
import com.nhnacademy.springailibrarystudy.personalization.domain.UserPreferenceVector;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingProperties;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

// 피드백과 임베딩을 조합해 개인화 계산을 위한 read model이라고 보기 때문에 personalization에 둠
@Repository
@RequiredArgsConstructor
public class PersonalizationQueryRepository {

    // ? 대신 이름 붙은 파라미터를 쓰게 해주는 JDBC 템플릿
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final OllamaEmbeddingProperties embeddingProperties;

    // 사용자 선호 벡터 계산: 최근 피드백 기반으로 LIKE, DISLIKE 벡터 평균 계산
    public UserPreferenceVector findUserPreferenceVector(String userKey, int recentLimit) {
        // latest_per_book: 책마다 최근 피드백 1개만 남김
        // limited_feedback: 거기서 LIKE, DISLIKE 각각 최근 N개만 남김
        // final select: LIKE, DISLIKE 각각 벡터 평균과 개수 계산
        String sql = """
                    with latest_per_book as (
                        select distinct on (sf.book_id)
                            sf.book_id,
                            sf.feedback_type,
                            sf.created_at
                        from search_feedbacks sf
                        where sf.user_key = :userKey
                          and sf.feedback_type in ('LIKE', 'DISLIKE')
                        order by sf.book_id, sf.created_at desc
                    ),
                    limited_feedback as (
                        select
                            book_id,
                            feedback_type
                        from (
                            select
                                book_id,
                                feedback_type,
                                created_at,
                                row_number() over (
                                    partition by feedback_type
                                    order by created_at desc
                                ) as rn
                            from latest_per_book
                        ) ranked_feedback
                        where rn <= :recentLimit
                    )
                    select
                        avg(be.embedding) filter (where lf.feedback_type = 'LIKE')::text as like_vector,
                        avg(be.embedding) filter (where lf.feedback_type = 'DISLIKE')::text as dislike_vector,
                        count(*) filter (where lf.feedback_type = 'LIKE') as like_count,
                        count(*) filter (where lf.feedback_type = 'DISLIKE') as dislike_count
                    from limited_feedback lf
                    join book_embeddings be on be.book_id = lf.book_id
                    where be.embedding_model = :embeddingModel
                """;

        // 파라미터 바인딩
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("userKey", userKey)
                .addValue("recentLimit", recentLimit)
                .addValue("embeddingModel", embeddingProperties.getModel());

        return namedParameterJdbcTemplate.queryForObject(
                sql,
                parameters,
                (rs, rowNum) -> new UserPreferenceVector(
                        userKey,
                        rs.getString("like_vector"),
                        rs.getString("dislike_vector"),
                        rs.getLong("like_count"),
                        rs.getLong("dislike_count")
                )
        );
    }

    // 후보 도서 별 사용자 선호도 유사도 계산: LIKE, DISLIKE 벡터와 후보 도서 임베딩 간 코사인 유사도 계산
    public List<CandidatePreferenceSimilarity> findCandidateSimilarities(
            List<Long> bookIds,
            String likeVector,
            String dislikeVector
    ) {
        // 중복 도서 제거
        List<Long> distinctBookIds = bookIds.stream()
                .distinct()
                .toList();
        // LIKE, DISLIKE 벡터가 모두 null이면 유사도 계산 불가
        if (distinctBookIds.isEmpty() || (likeVector == null && dislikeVector == null)) {
            return List.of();
        }

        // cosine distance를 유사도로 사용하기 위해: 1 - distance (0 ~ 2) -> (1 ~ -1)
        String sql = """
            select
                be.book_id,
                1 - (be.embedding <=> cast(:likeVector as vector)) as like_similarity,
                1 - (be.embedding <=> cast(:dislikeVector as vector)) as dislike_similarity
            from book_embeddings be
            where be.embedding_model = :embeddingModel
              and be.book_id in (:bookIds)
        """;

        // 파라미터 바인딩
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("likeVector", likeVector)
                .addValue("dislikeVector", dislikeVector)
                .addValue("embeddingModel", embeddingProperties.getModel())
                .addValue("bookIds", distinctBookIds);

        return namedParameterJdbcTemplate.query(
                sql,
                parameters,
                (rs, rowNum) -> new CandidatePreferenceSimilarity(
                    rs.getLong("book_id"),
                    rs.getObject("like_similarity", Double.class),
                    rs.getObject("dislike_similarity", Double.class)
                )
        );
    }
}
