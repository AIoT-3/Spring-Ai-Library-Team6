package com.nhnacademy.springailibrarystudy.rag.application.dto;

import com.nhnacademy.springailibrarystudy.search.domain.SearchType;

public record GenerateRagAnswerCommand(
        String question,
        String userKey,
        int topK,
        SearchType searchType
) {

    private static final int DEFAULT_TOP_K = 5;

    public GenerateRagAnswerCommand {
        question = normalize(question);
        userKey = normalize(userKey);
        topK = topK <= 0 ? DEFAULT_TOP_K : topK;
        searchType = searchType == null ? SearchType.KEYWORD : searchType;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
