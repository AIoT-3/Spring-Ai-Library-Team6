package com.nhnacademy.springailibrarystudy.book.infrastructure.runner;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "book.embedding.generation")
public record BookEmbeddingGenerationProperties(
        boolean enabled,
        int batchSize,
        int rowLimit
) {
}
