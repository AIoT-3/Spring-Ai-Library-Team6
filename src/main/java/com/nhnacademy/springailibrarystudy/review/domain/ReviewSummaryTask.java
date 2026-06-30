package com.nhnacademy.springailibrarystudy.review.domain;

public record ReviewSummaryTask(Long bookId, long enqueueTime) {

    public static ReviewSummaryTask of(Long bookId) {
        return new ReviewSummaryTask(bookId, System.currentTimeMillis());
    }
}