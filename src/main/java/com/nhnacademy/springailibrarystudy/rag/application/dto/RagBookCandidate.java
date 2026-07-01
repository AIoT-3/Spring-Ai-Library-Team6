package com.nhnacademy.springailibrarystudy.rag.application.dto;

import java.math.BigDecimal;

// 벡터 검색으로 db에서 검색된, llm에게 넘길 도서 후보.
public record RagBookCandidate(
        Long id,
        String isbn13,
        String title,
        String authorName,
        String publisherName,
        String description,
        String imageUrl,
        Double similarity,
        Double rrfScore,
        // 리뷰 정보
        BigDecimal averageRating,
        Long reviewCount,
        String reviewSummary

) {
    public RagBookCandidate withReviewInfo(BigDecimal averageRating, Long reviewCount, String reviewSummary) {
        return new RagBookCandidate(
                id, isbn13, title, authorName, publisherName, description, imageUrl,
                similarity, rrfScore, averageRating, reviewCount, reviewSummary
        );
    }
}