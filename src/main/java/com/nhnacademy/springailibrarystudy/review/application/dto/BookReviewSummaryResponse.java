package com.nhnacademy.springailibrarystudy.review.application.dto;

public record BookReviewSummaryResponse(
        Long bookId,
        String summary,
        int reviewCount,
        Double averageRating
) {
}
