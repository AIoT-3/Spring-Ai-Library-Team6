package com.nhnacademy.springailibrarystudy.rag.application.dto;

import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import java.util.List;

public record GenerateRagAnswerResult(
        String answerId,
        String answer,
        List<BookSearchItemResponse> sources,
        boolean cached
) {

    public GenerateRagAnswerResult {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
