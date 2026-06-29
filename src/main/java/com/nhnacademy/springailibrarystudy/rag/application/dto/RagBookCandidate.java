package com.nhnacademy.springailibrarystudy.rag.application.dto;

// 벡터 검색으로 db에서 검색된, llm에게 넘길 도서 후보.
public record RagBookCandidate(
        Long id,
        String isbn13,
        String title,
        String authorName,
        String publisherName,
        String description,
        String imageUrl,
        Double similarity
) {
}
