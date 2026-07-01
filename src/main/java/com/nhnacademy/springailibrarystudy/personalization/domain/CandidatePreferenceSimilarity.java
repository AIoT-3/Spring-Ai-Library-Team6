package com.nhnacademy.springailibrarystudy.personalization.domain;

/**
 * 검색 후보 책 하나가 사용자의 선호도와 얼마나 유사한지
 * @param bookId 후보 책 ID
 * @param likeSimilarity 사용자가 좋아요를 누른 책들과의 유사도 (-1.0 ~ 1.0)
 * @param dislikeSimilarity 사용자가 싫어요를 누른 책들과의 유사도 (-1.0 ~ 1.0)
 */
public record CandidatePreferenceSimilarity(
        Long bookId,
        Double likeSimilarity,
        Double dislikeSimilarity
) {
    public double likeSignal() {
        return likeSimilarity == null ? 0.0 : likeSimilarity;
    }

    public double dislikeSignal() {
        return dislikeSimilarity == null ? 0.0 : dislikeSimilarity;
    }
}
