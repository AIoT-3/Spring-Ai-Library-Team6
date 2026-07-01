package com.nhnacademy.springailibrarystudy.review.infrastructure;

import com.nhnacademy.springailibrarystudy.review.domain.BookReview;
import com.nhnacademy.springailibrarystudy.review.domain.QBookReview;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BookReviewRepositoryCustomImpl implements BookReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BookReview> findNewReviewsAfterId(Long bookId, Long lastSummarizedReviewId) {
        QBookReview r = QBookReview.bookReview;
        return queryFactory
                .selectFrom(r)
                .where(r.book.id.eq(bookId)
                        .and(r.id.gt(lastSummarizedReviewId)))
                .orderBy(r.id.asc())
                .fetch();
    }

    @Override
    public List<BookReview> findAllByBookId(Long bookId) {
        QBookReview r = QBookReview.bookReview;
        return queryFactory
                .selectFrom(r)
                .where(r.book.id.eq(bookId))
                .orderBy(r.id.asc())
                .fetch();
    }
}