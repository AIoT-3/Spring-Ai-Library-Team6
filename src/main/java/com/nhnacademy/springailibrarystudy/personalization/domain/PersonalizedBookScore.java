package com.nhnacademy.springailibrarystudy.personalization.domain;

/**
 * 검색 후보 책 하나의 최종 점수 계산 대상
 * @param bookId 후보 책 ID
 * @param normalizedRrfScore RRF 점수 정규화 값
 * @param likeSimilarity 사용자가 좋아요를 누른 책들과의 유사도
 * @param dislikeSimilarity 사용자가 싫어요를 누른 책들과의 유사도
 */
public record PersonalizedBookScore(
        Long bookId,
        double normalizedRrfScore,
        double likeSimilarity,
        double dislikeSimilarity
) {
    // 최종 점수 계산
    public double finalScore(PersonalizationScoringPolicy policy) {
        return policy.calculate(normalizedRrfScore, likeSimilarity, dislikeSimilarity);
    }
}
