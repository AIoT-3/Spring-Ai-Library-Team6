package com.nhnacademy.springailibrarystudy.personalization.domain;

import java.io.Serializable;

/**
 * 사용자가 좋아요,싫어요를 누른 책들의 평균 벡터
 * @param userKey 사용자 식별자 (e.g.텔레그램 chatId)
 * @param likeVector pgvector literal
 * @param dislikeVector pgvector literal
 * @param likeCount 좋아요 수
 * @param dislikeCount 싫어요 수
 */
public record UserPreferenceVector(
        String userKey,

        String likeVector,
        String dislikeVector,

        long likeCount,
        long dislikeCount

) implements Serializable {

    // 개인화 추천을 위한 최소 좋아요, 싫어요 수
    private static final int MIN_LIKE_COUNT = 3;
    private static final int MIN_DISLIKE_COUNT = 3;

    // 개인화 추천 가능 여부 판단
    public boolean personalizable() {
        return (likeVector != null && likeCount >= MIN_LIKE_COUNT)
            || (dislikeVector != null && dislikeCount >= MIN_DISLIKE_COUNT);
    }
}