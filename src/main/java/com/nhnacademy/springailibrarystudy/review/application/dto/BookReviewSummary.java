package com.nhnacademy.springailibrarystudy.review.application.dto;

public record BookReviewSummary(
        Long bookId,
        String summary,
        int reviewCount,
        Double averageRating
) {
}
