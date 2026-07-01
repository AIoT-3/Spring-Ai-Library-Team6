package com.nhnacademy.springailibrarystudy.personalization.domain;

/**
 * 개인화 점수의 계산 정책
 * @param rrfWeight RRF 점수 가중치
 * @param likeWeight 좋아요 유사도 가중치
 * @param dislikeWeight 싫어요 유사도 가중치
 */
public record PersonalizationScoringPolicy(
        double rrfWeight,
        double likeWeight,
        double dislikeWeight
) {
    public static PersonalizationScoringPolicy defaults() {
        return new PersonalizationScoringPolicy(
                0.7,
                0.25,
                0.05
        );
    }

    public double calculate(
            double normalizedRrf,
            double likeSimilarity,
            double dislikeSimilarity
    ) {
        // 최종 점수 계산: RRF 점수 + 좋아요 유사도 - 싫어요 유사도
        // + 좋아요: 좋아한 것과 유사하면 가점, 좋아한 것과 멀거나 반대면 감점
        // - 싫어요: 싫어한 것과 유사하면 감점, 싫어한 것과 멀거나 반대면 가점
        return normalizedRrf * rrfWeight
                + likeSimilarity * likeWeight
                - dislikeSimilarity * dislikeWeight;
    }
}
