package com.nhnacademy.springailibrarystudy.rag.application.dto;

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
                recommendationReason
        );
    }
}
