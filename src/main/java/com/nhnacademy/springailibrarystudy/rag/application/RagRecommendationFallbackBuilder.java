package com.nhnacademy.springailibrarystudy.rag.application;

import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookRecommendation;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RagRecommendationFallbackBuilder {

    private static final int MAX_REASON_SOURCE_LENGTH = 512;

    public List<RagBookRecommendation> build(List<RagBookCandidate> candidates, int recommendationTopK) {
        return candidates.stream()
                .limit(recommendationTopK)
                .map(candidate -> RagBookRecommendation.of(candidate, reason(candidate)))
                .toList();
    }

    private String reason(RagBookCandidate candidate) {
        if (StringUtils.hasText(candidate.description())) {
            return """
                    LLM 추천 생성에 실패하여 검색 결과 순서대로 표시합니다. 책 소개: %s
                    """.formatted(trim(candidate.description())).trim();
        }

        return "LLM 추천 생성에 실패하여 검색 결과 순서대로 표시합니다.";
    }

    private String trim(String value) {
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= MAX_REASON_SOURCE_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_REASON_SOURCE_LENGTH).trim() + "...";
    }
}
