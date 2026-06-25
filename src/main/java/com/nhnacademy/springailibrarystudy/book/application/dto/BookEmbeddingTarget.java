package com.nhnacademy.springailibrarystudy.book.application.dto;

public record BookEmbeddingTarget(
        Long bookId,
        String title,
        String volumeTitle,
        String authorName,
        String publisherName,
        String description
) {
}
