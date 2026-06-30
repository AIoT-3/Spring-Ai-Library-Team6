package com.nhnacademy.springailibrarystudy.review.domain;

import java.time.LocalDateTime;

public record ReviewStatistics(
        long reviewCount,
        Double averageRating,
        long rating1Count,
        long rating2Count,
        long rating3Count,
        long rating4Count,
        long rating5Count,
        LocalDateTime lastReviewedAt
) {
}