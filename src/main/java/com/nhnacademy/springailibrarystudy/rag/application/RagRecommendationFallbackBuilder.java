package com.nhnacademy.springailibrarystudy.rag.application;

import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookRecommendation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RagRecommendationFallbackBuilder {
    // 코드변경점. llm 추천 생성에 실패하였을 때, 사용자에게 이중으로 안내되어 불필요한 설명이라는 느낌을 받을 수 있던 부분을
    // 상위 ? 위 도서라는 표기로 불필요하게 반복되는 안내를 바꾸었음
    public List<RagBookRecommendation> build(List<RagBookCandidate> candidates, int recommendationTopK) {
        List<RagBookCandidate> selected = candidates.stream().limit(recommendationTopK).toList();

        List<RagBookRecommendation> recommendations = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            recommendations.add(RagBookRecommendation.of(selected.get(i), reason(i + 1)));
        }
        return recommendations;
    }

    private String reason(int rank) {
        return "검색 결과 상위 %d위 도서입니다.".formatted(rank);
    }
}
