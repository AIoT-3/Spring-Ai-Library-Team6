package com.nhnacademy.springailibrarystudy.book.application.dto;

import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import org.springframework.util.StringUtils;

public record BookEmbeddingGenerationOptions(
        String embeddingModel,
        int batchSize,
        int rowLimit
) {

    public BookEmbeddingGenerationOptions {
        if (!StringUtils.hasText(embeddingModel)
                || batchSize <= 0
                || rowLimit < 0
        ) {
            throw new BusinessException(ErrorCode.INVALID_EMBEDDING_GENERATION_OPTIONS);
        }

        embeddingModel = embeddingModel.trim();
    }
}
