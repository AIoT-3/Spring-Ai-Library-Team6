package com.nhnacademy.springailibrarystudy.book.infrastructure.persistence;

import com.nhnacademy.springailibrarystudy.book.domain.QBook;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class BookQueryRepository {

    private static final QBook book = QBook.book;

    private final JdbcTemplate jdbcTemplate;
    private final JPAQueryFactory queryFactory;
    private final OllamaEmbeddingProperties embeddingProperties;

    public Page<BookSearchItemResponse> searchByKeyword(
            String query, String isbn13, String kdcCode, Pageable pageable
    ) {
        // 동적 where 조립용 객체인 BooleanBuilder를 사용하여 검색 조건을 구성
        BooleanBuilder where = whereKeywordMatches(query, isbn13, kdcCode);

        // 검색 조건에 맞는 도서 정보를 projection
        List<BookSearchItemResponse> content = queryFactory
                .select(Projections.constructor(
                        BookSearchItemResponse.class,
                        book.id,
                        book.volumeTitle,
                        book.title,
                        book.authorName,
                        book.publisherName,
                        book.publishedDate,
                        book.price,
                        book.imageUrl,
                        Expressions.nullExpression(Double.class)
                ))
                .from(book)
                .where(where)
                .orderBy(book.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch(); // fetch()는 결과를 리스트로 반환하며, 결과가 없으면 빈 리스트를 반환

        // 검색 조건에 맞는 도서의 총 개수를 조회
        Long total = queryFactory
                .select(book.count())
                .from(book)
                .where(where)
                .fetchOne(); // fetchOne()는 단일 결과를 반환하며, 결과가 없으면 null을 반환

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanBuilder whereKeywordMatches(String query, String isbn13, String kdcCode) {
        BooleanBuilder builder = new BooleanBuilder();

        // OR 조건: query가 조건들 중 하나라도 포함되면 검색 결과에 포함
        if (StringUtils.hasText(query)) {
            builder.andAnyOf(
                    book.title.containsIgnoreCase(query),
                    book.volumeTitle.containsIgnoreCase(query),
                    book.authorName.containsIgnoreCase(query),
                    book.publisherName.containsIgnoreCase(query)
            );

            BooleanExpression fts = Expressions.booleanTemplate(
                    "function('ts_match_korean', {0}, {1}) = true",
                    book.description,  // 검색 대상 필드
                    query           // 검색어
            );
            builder.or(fts);
        }

        // AND 조건: isbn13이 정확히 일치하는 경우만 검색 결과에 포함
        if (StringUtils.hasText(isbn13)) {
            builder.and(book.isbn13.eq(isbn13));
        }

        // AND 조건: kdcCode의 prefix가 일치하는 경우만 검색 결과에 포함
        if (StringUtils.hasText(kdcCode)) {
            builder.and(book.kdcCode.startsWith(kdcCode));
        }

        return builder;
    }

    public Page<BookSearchItemResponse> vectorSearch(Pageable pageable, float[] vector) {
        String vectorString = arrayToVectorString(vector);
        String model = embeddingProperties.getModel();

        List<BookSearchItemResponse> results = jdbcTemplate.query(
                """
                SELECT
                    b.id,
                    b.isbn13,
                    b.volume_title,
                    b.title,
                    b.author_name,
                    b.publisher_name,
                    b.published_date,
                    b.price,
                    b.image_url,
                    b.description,
                    1 - (be.embedding <=> ?::vector) AS similarity
                FROM book_embeddings be
                JOIN books b ON b.id = be.book_id
                WHERE be.embedding_model = ?
                ORDER BY be.embedding <=> ?::vector
                LIMIT ? OFFSET ?
                """,
                (rs, rowNum) -> new BookSearchItemResponse(
                        rs.getLong("id"),
                        rs.getString("isbn13"),
                        rs.getString("volume_title"),
                        rs.getString("title"),
                        rs.getString("author_name"),
                        rs.getString("publisher_name"),
                        rs.getObject("published_date", LocalDate.class),
                        rs.getBigDecimal("price"),
                        rs.getString("image_url"),
                        rs.getString("description"),
                        rs.getDouble("similarity")
                ),
                vectorString, model, vectorString, pageable.getPageSize(), pageable.getOffset()
        );

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM book_embeddings WHERE embedding_model = ?",
                Long.class,
                model
        );

        return new PageImpl<>(results, pageable, total == null ? 0 : total);
    }

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
