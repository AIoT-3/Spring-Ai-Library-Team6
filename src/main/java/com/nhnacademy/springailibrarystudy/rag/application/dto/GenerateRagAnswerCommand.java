package com.nhnacademy.springailibrarystudy.rag.application.dto;

public record GenerateRagAnswerCommand(
        // 사용자 자연어 질문 -> 후보 검색 -> LLM 추천
        String question,
        // 개인화/피드백 연동 시 사용할 사용자 식별자? 또는 개인화 프롬프트
        String userKey,
        // DB search에서 가져올 후보 수
        int candidateTopK,
        // LLM이 최종 추천할 도서 수
        int recommendationTopK
) {

    private static final int DEFAULT_CANDIDATE_TOP_K = 10;
    private static final int MAX_CANDIDATE_TOP_K = 10;
    private static final int DEFAULT_RECOMMENDATION_TOP_K = 5;
    private static final int MAX_RECOMMENDATION_TOP_K = 5;

    public GenerateRagAnswerCommand {
        question = normalize(question);
        userKey = normalize(userKey);
        candidateTopK = candidateTopK <= 0
                ? DEFAULT_CANDIDATE_TOP_K
                : Math.min(candidateTopK, MAX_CANDIDATE_TOP_K);
        recommendationTopK = recommendationTopK <= 0
                ? DEFAULT_RECOMMENDATION_TOP_K
                : Math.min(recommendationTopK, MAX_RECOMMENDATION_TOP_K);
    }

    public static GenerateRagAnswerCommand of(String question) {
        return new GenerateRagAnswerCommand(
                question, null, DEFAULT_CANDIDATE_TOP_K, DEFAULT_RECOMMENDATION_TOP_K);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
