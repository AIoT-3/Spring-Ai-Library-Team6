package com.nhnacademy.springailibrarystudy.book.infrastructure.runner;

import com.nhnacademy.springailibrarystudy.book.application.GenerateBookEmbeddingsUseCase;
import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingGenerationOptions;
import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingGenerationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(
        prefix = "book.embedding.generation",
        name = "enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class BookEmbeddingGenerationApplicationRunner implements ApplicationRunner {

    private final BookEmbeddingGenerationProperties properties;
    private final GenerateBookEmbeddingsUseCase generateBookEmbeddingsUseCase;
    private final OllamaEmbeddingProperties embeddingProperties;

    @Override
    public void run(ApplicationArguments args) {
        // Book embedding 옵션 생성
        BookEmbeddingGenerationOptions options = new BookEmbeddingGenerationOptions(
                embeddingProperties.getModel(),
                properties.batchSize(),
                properties.rowLimit()
        );

        // Book embedding 생성 실행
        BookEmbeddingGenerationResult result = generateBookEmbeddingsUseCase.generate(options);

        log.info(
                "Book embedding 생성 완료. model={}, batchSize={}, rowLimit={}, targetCount={}, insertedCount={}, elapsedMillis={}",
                result.embeddingModel(),
                result.batchSize(),
                result.rowLimit(),
                result.targetCount(),
                result.insertedCount(),
                result.elapsedMillis()
        );
    }
}
