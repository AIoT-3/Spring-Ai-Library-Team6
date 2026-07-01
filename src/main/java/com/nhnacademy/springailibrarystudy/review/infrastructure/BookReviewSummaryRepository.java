package com.nhnacademy.springailibrarystudy.review.infrastructure;

import com.nhnacademy.springailibrarystudy.review.domain.BookReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReviewSummaryRepository extends JpaRepository<BookReviewSummary, Long> {
}
