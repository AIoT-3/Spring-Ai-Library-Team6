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

    private static final int DEFAULT_CANDIDATE_TOP_K = 100;
    private static final int DEFAULT_RECOMMENDATION_TOP_K = 10;

    /**
     * GenerateRagAnswerCommand.of(question)을 호출 시,
     * 두 필드(candidateTopK, recommendationTopK)가 디폴트 값으로 지정되도록 생성자 변경
     * @param question 사용자 질문 (자연어)
     * @param userKey ()
     * @param candidateTopK DB search(hybrid-search)로 가져올 후보 수
     * @param recommendationTopK LLM이 최종 추천할 도서 수
     */
    public GenerateRagAnswerCommand(String question, String userKey, int candidateTopK, int recommendationTopK) {
        this.question = normalize(question);
        this.userKey = normalize(userKey);
        this.candidateTopK = candidateTopK;
        this.recommendationTopK = recommendationTopK;
    }

    public static GenerateRagAnswerCommand of(String question, String userkey) {
        return new GenerateRagAnswerCommand(
                question, userkey, DEFAULT_CANDIDATE_TOP_K, DEFAULT_RECOMMENDATION_TOP_K);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
