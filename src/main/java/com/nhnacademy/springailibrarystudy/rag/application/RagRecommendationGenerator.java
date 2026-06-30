package com.nhnacademy.springailibrarystudy.rag.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookRecommendation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagRecommendationGenerator {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public List<RagBookRecommendation> generate(
            Prompt prompt,
            List<RagBookCandidate> candidates,
            int recommendationTopK
    ) {
        log.info("LLM 추천 호출: 후보 {}건, topK={}", candidates.size(), recommendationTopK);
        long start = System.currentTimeMillis();
        String response = chatModel.call(prompt).getResult().getOutput().getText();
        log.info("LLM 응답 수신: {}자, elapsed={}ms",
                response == null ? 0 : response.length(), System.currentTimeMillis() - start);
        log.debug("LLM 원문 응답: {}", response);

        List<ParsedRecommendation> parsedRecommendations = parse(response);
        Map<Long, RagBookCandidate> candidateById = toCandidateMap(candidates);

        List<RagBookRecommendation> recommendations = parsedRecommendations.stream()
                .filter(parsed -> candidateById.containsKey(parsed.id()))
                .limit(recommendationTopK)
                .map(parsed -> RagBookRecommendation.of(
                        candidateById.get(parsed.id()),
                        parsed.recommendationReason()
                ))
                .toList();

        log.info("LLM 추천 파싱 완료: 파싱 {}건 -> 후보 매칭 {}건",
                parsedRecommendations.size(), recommendations.size());
        if (recommendations.isEmpty()) {
            log.warn("LLM 추천 매칭 0건, 폴백 예정. 원문 응답: {}", response);
        }
        return recommendations;
    }

    private Map<Long, RagBookCandidate> toCandidateMap(List<RagBookCandidate> candidates) {
        Map<Long, RagBookCandidate> candidateById = new LinkedHashMap<>();
        for (RagBookCandidate candidate : candidates) {
            candidateById.put(candidate.id(), candidate);
        }
        return candidateById;
    }

    private List<ParsedRecommendation> parse(String response) {
        try {
            JsonNode array = objectMapper.readTree(extractJsonArray(response));
            if (!array.isArray()) {
                return List.of();
            }

            List<ParsedRecommendation> parsed = new ArrayList<>();
            for (JsonNode item : array) {
                JsonNode id = item.get("id");
                JsonNode reason = item.get("recommendationReason");
                if (id == null || !id.canConvertToLong() || reason == null || reason.isNull()) {
                    continue;
                }
                String text = reason.asText().trim();
                if (!text.isEmpty()) {
                    parsed.add(new ParsedRecommendation(id.asLong(), text));
                }
            }
            return parsed;
        } catch (Exception e) {
            log.warn("LLM 응답 JSON 파싱 실패: {}", e.toString());
            return List.of();
        }
    }

    private String extractJsonArray(String response) {
        if (!StringUtils.hasText(response)) {
            return "[]";
        }

        // think=false로도 thinking 출력이 남는 경우 </think> 이후 본문만 사용하ㅁ
        int thinkEnd = response.lastIndexOf("</think>");
        String body = thinkEnd >= 0 ? response.substring(thinkEnd + "</think>".length()) : response;

        int start = body.indexOf('[');
        int end = body.lastIndexOf(']');
        if (start < 0 || end < start) {
            return "[]";
        }
        return body.substring(start, end + 1);
    }

    private record ParsedRecommendation(
            Long id,
            String recommendationReason
    ) {
    }
}
