package com.nhnacademy.springailibrarystudy.rag.application.dto;

import com.nhnacademy.springailibrarystudy.search.domain.SearchType;

public record GenerateRagAnswerCommand(
        // 사용자 자연어 질문 -> 임베딩 -> 검색
        String question,
        // 개인화/피드백 연동 시 사용할 사용자 식별자? 또는 개인화 프롬프트
        String userKey,
        // DB vector search에서 가져올 후보 수
        int candidateTopK,
        // LLM이 최종 추천할 도서 수
        int recommendationTopK,
        SearchType searchType
) {

    private static final int DEFAULT_CANDIDATE_TOP_K = 10;
    private static final int MAX_CANDIDATE_TOP_K = 10;
    private static final int DEFAULT_RECOMMENDATION_TOP_K = 5;
    private static final int MAX_RECOMMENDATION_TOP_K = 5;

    public GenerateRagAnswerCommand(String question, String userKey, int topK, SearchType searchType) {
        this(question, userKey, DEFAULT_CANDIDATE_TOP_K, topK, searchType);
    }

    public GenerateRagAnswerCommand {
        question = normalize(question);
        userKey = normalize(userKey);
        candidateTopK = candidateTopK <= 0
                ? DEFAULT_CANDIDATE_TOP_K
                : Math.min(candidateTopK, MAX_CANDIDATE_TOP_K);
        recommendationTopK = recommendationTopK <= 0
                ? DEFAULT_RECOMMENDATION_TOP_K
                : Math.min(recommendationTopK, MAX_RECOMMENDATION_TOP_K);
        searchType = searchType == null ? SearchType.VECTOR : searchType;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
