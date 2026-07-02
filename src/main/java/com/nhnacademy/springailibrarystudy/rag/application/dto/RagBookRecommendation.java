package com.nhnacademy.springailibrarystudy.rag.application.dto;

import java.math.BigDecimal;

// llm에 의해 추천된 도서
public record RagBookRecommendation(
        Long id,
        String isbn13,
        String title,
        String authorName,
        String publisherName,
        String description,
        String imageUrl,
        Double similarity,
        Double rrfScore,
        BigDecimal averageRating,
        Long reviewCount,
        String reviewSummary,
        String recommendationReason
) {

    public static RagBookRecommendation of(RagBookCandidate candidate, String recommendationReason) {
        return new RagBookRecommendation(
                candidate.id(),
                candidate.isbn13(),
                candidate.title(),
                candidate.authorName(),
                candidate.publisherName(),
                candidate.description(),
                candidate.imageUrl(),
                candidate.similarity(),
                candidate.rrfScore(),
                candidate.averageRating(),
                candidate.reviewCount(),
                candidate.reviewSummary(),
                recommendationReason
        );
    }
}
