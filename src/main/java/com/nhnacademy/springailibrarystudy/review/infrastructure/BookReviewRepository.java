package com.nhnacademy.springailibrarystudy.review.infrastructure;

import com.nhnacademy.springailibrarystudy.review.domain.BookReview;
import com.nhnacademy.springailibrarystudy.review.domain.ReviewStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookReviewRepository
        extends JpaRepository<BookReview, Long>, BookReviewRepositoryCustom {

    @Query("""
            SELECT new com.nhnacademy.springailibrarystudy.review.domain.ReviewStatistics(
                COUNT(r),
                AVG(r.rating),
                SUM(CASE WHEN r.rating = 1 THEN 1L ELSE 0L END),
                SUM(CASE WHEN r.rating = 2 THEN 1L ELSE 0L END),
                SUM(CASE WHEN r.rating = 3 THEN 1L ELSE 0L END),
                SUM(CASE WHEN r.rating = 4 THEN 1L ELSE 0L END),
                SUM(CASE WHEN r.rating = 5 THEN 1L ELSE 0L END),
                MAX(r.createdAt)
            )
            FROM BookReview r
            WHERE r.book.id = :bookId
            """)
    ReviewStatistics aggregateByBookId(@Param("bookId") Long bookId);
}