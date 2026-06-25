package com.nhnacademy.springailibrarystudy.book.application.dto;

public record BookEmbeddingGenerationResult(
        String embeddingModel,
        int batchSize,
        int rowLimit,
        int targetCount,
        int insertedCount,
        long elapsedMillis
) {
}
