package com.nhnacademy.springailibrarystudy.review.infrastructure;

import com.nhnacademy.springailibrarystudy.review.domain.BookReview;

import java.util.List;

public interface BookReviewRepositoryCustom {

    List<BookReview> findNewReviewsAfterId(Long bookId, Long lastSummarizedReviewId);

    List<BookReview> findAllByBookId(Long bookId);
}