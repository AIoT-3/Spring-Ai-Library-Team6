package com.nhnacademy.springailibrarystudy.rag.application.dto;

import java.util.List;

public record GenerateRagAnswerResult(
        String answerId,
        String answer,
        List<RagBookRecommendation> books,
        boolean cached,
        boolean fallback
) {

    public GenerateRagAnswerResult {
        books = books == null ? List.of() : List.copyOf(books);
    }
}
