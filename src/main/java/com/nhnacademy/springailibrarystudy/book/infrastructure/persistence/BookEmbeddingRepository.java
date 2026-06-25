package com.nhnacademy.springailibrarystudy.book.infrastructure.persistence;

import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingTarget;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class BookEmbeddingRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<BookEmbeddingTarget> findCandidateTargetsAfter(long lastBookId, int limit) {
        // source text hash는 애플리케이션 전처리 결과라서, 여기서는 도서 후보만 id 기준으로 나눠 조회
        return jdbcTemplate.query(
                """
                select
                    b.id,
                    b.title,
                    b.volume_title,
                    b.author_name,
                    b.publisher_name,
                    b.description
                from books b
                where b.id > ?
                order by b.id
                limit ?
                """,
                (rs, rowNum) -> new BookEmbeddingTarget(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("volume_title"),
                        rs.getString("author_name"),
                        rs.getString("publisher_name"),
                        rs.getString("description")
                ),
                lastBookId,
                limit
        );
    }

    public Set<Long> findExistingBookIds(String embeddingModel, Map<Long, String> sourceTextHashByBookId) {
        if (sourceTextHashByBookId.isEmpty()) {
            return Set.of();
        }

        StringBuilder values = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        int index = 0;

        // 후보별 book id와 source hash를 values 테이블로 만들어 한 번의 쿼리로 기존 임베딩과 비교
        for (Map.Entry<Long, String> entry : sourceTextHashByBookId.entrySet()) {
            if (index++ > 0) {
                values.append(", ");
            }
            values.append("(?, ?)");
            parameters.add(entry.getKey());
            parameters.add(entry.getValue());
        }
        parameters.add(embeddingModel);

        String sql = """
                select be.book_id
                from book_embeddings be
                join (values %s) as target(book_id, source_text_hash)
                  on target.book_id = be.book_id
                 and target.source_text_hash = be.source_text_hash
                where be.embedding_model = ?
                """.formatted(values);

        return new HashSet<>(jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("book_id"),
                parameters.toArray()
        ));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public int batchInsert(
            String embeddingModel,
            List<BookEmbeddingTarget> targets,
            List<String> sourceTexts,
            List<String> sourceTextHashes,
            List<float[]> embeddings
    ) {
        if (targets.isEmpty()) {
            return 0;
        }

        // 재실행 또는 동시 실행 중 중복이 생겨도 unique key 충돌은 저장 생략으로 처리
        int[] updateCounts = jdbcTemplate.batchUpdate(
                """
                insert into book_embeddings (
                    id,
                    book_id,
                    embedding_model,
                    source_text_hash,
                    source_text,
                    embedding,
                    created_at
                )
                values (
                    nextval('book_embedding_sequence'),
                    ?,
                    ?,
                    ?,
                    ?,
                    ?::vector,
                    current_timestamp
                )
                on conflict (book_id, embedding_model, source_text_hash) do nothing
                """,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, targets.get(i).bookId());
                        ps.setString(2, embeddingModel);
                        ps.setString(3, sourceTextHashes.get(i));
                        ps.setString(4, sourceTexts.get(i));
                        ps.setString(5, toVectorLiteral(embeddings.get(i)));
                    }

                    @Override
                    public int getBatchSize() {
                        return targets.size();
                    }
                }
        );

        return countInsertedRows(updateCounts);
    }

    private int countInsertedRows(int[] updateCounts) {
        int insertedCount = 0;
        for (int updateCount : updateCounts) {
            if (updateCount > 0 || updateCount == Statement.SUCCESS_NO_INFO) {
                insertedCount++;
            }
        }
        return insertedCount;
    }

    private String toVectorLiteral(float[] embedding) {
        // pgvector 컬럼에 넣기 위해 [0.1,0.2,...] 형태의 vector literal로 변환
        StringBuilder vectorLiteral = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                vectorLiteral.append(',');
            }
            vectorLiteral.append(embedding[i]);
        }
        return vectorLiteral.append(']').toString();
    }
}
