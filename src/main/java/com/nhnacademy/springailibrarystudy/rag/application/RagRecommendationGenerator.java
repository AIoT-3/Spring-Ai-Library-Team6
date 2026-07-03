package com.nhnacademy.springailibrarystudy.rag.application;

import com.fasterxml.jackson.core.json.JsonReadFeature;
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
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
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
        ChatResponse chatResponse = chatModel.call(prompt);
        String response = chatResponse.getResult().getOutput().getText();
        Usage usage = chatResponse.getMetadata().getUsage();
        log.info("LLM 응답 수신: {}자, elapsed={}ms, tokens(prompt={}, completion={}, total={})",
                response == null ? 0 : response.length(), System.currentTimeMillis() - start,
                usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
        log.info("LLM 원문 응답: {}", response);

        List<ParsedRecommendation> parsedRecommendations = parse(response);
        Map<Long, RagBookCandidate> candidateById = toCandidateMap(candidates);
        List<RagBookRecommendation> recommendations = selectRecommendations(
                parsedRecommendations, candidateById, recommendationTopK);

        log.info("LLM 추천 파싱 완료: 파싱 {}건 -> 후보 매칭 {}건",
                parsedRecommendations.size(), recommendations.size());
        if (recommendations.isEmpty()) {
            log.warn("LLM 추천 매칭 0건, 폴백 예정.");
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

    // recommended=false, 후보 목록에 없는 id, topK 초과 등 최종 추천에서 제외되는 항목을 로그로 남김
    private List<RagBookRecommendation> selectRecommendations(
            List<ParsedRecommendation> parsedRecommendations,
            Map<Long, RagBookCandidate> candidateById,
            int recommendationTopK
    ) {
        List<RagBookRecommendation> recommendations = new ArrayList<>();
        for (ParsedRecommendation parsed : parsedRecommendations) {
            if (!parsed.recommended()) {
                log.info("추천 제외(recommended=false): id={}, reason={}", parsed.id(), parsed.recommendationReason());
                continue;
            }

            RagBookCandidate candidate = candidateById.get(parsed.id());
            if (candidate == null) {
                log.warn("추천 제외(후보 목록에 없는 id): id={}, reason={}", parsed.id(), parsed.recommendationReason());
                continue;
            }

            if (recommendations.size() >= recommendationTopK) {
                log.info("추천 제외(topK={} 초과): id={}, reason={}",
                        recommendationTopK, parsed.id(), parsed.recommendationReason());
                continue;
            }

            recommendations.add(RagBookRecommendation.of(candidate, parsed.recommendationReason()));
        }
        return recommendations;
    }

    // 배열 전체를 한 번에 파싱하면 항목 하나의 포맷 오류로 전체가 실패하므로,
    // 항목({...}) 단위로 나눠 개별적으로 파싱을 도입
    private List<ParsedRecommendation> parse(String response) {
        List<String> objectTexts = splitTopLevelObjects(extractJsonArray(response));
        if (objectTexts.isEmpty()) {
            log.warn("LLM 응답에서 파싱 가능한 JSON 객체를 찾지 못함");
            return List.of();
        }

        List<ParsedRecommendation> parsed = new ArrayList<>();
        for (String objectText : objectTexts) {
            ParsedRecommendation recommendation = parseObject(objectText);
            if (recommendation != null) {
                parsed.add(recommendation);
            }
        }
        return parsed;
    }

    private ParsedRecommendation parseObject(String objectText) {
        try {
            // format=json 제약을 걸어도 모델이 문자열을 홑따옴표로 감싸는 경우가 있어 고려하여 파싱
            JsonNode item = objectMapper.reader()
                    .with(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature())
                    .readTree(objectText);

            JsonNode id = item.get("id");
            JsonNode reason = item.get("recommendationReason");
            if (id == null || !id.canConvertToLong() || reason == null || reason.isNull()) {
                log.warn("LLM 응답 항목에 id/recommendationReason 필드가 없어 제외: {}", objectText);
                return null;
            }

            String text = reason.asText().trim();
            if (text.isEmpty()) {
                log.warn("LLM 응답 항목의 recommendationReason이 비어있어 제외: {}", objectText);
                return null;
            }

            boolean recommended = item.path("recommended").asBoolean(false);
            return new ParsedRecommendation(id.asLong(), recommended, text);
        } catch (Exception e) {
            log.warn("LLM 응답 항목 JSON 파싱 실패, 해당 항목만 제외: {} ({})", objectText, e.toString());
            return null;
        }
    }

    // <think> 태그와 배열 앞뒤 텍스트를 걷어내 { ... } 후보 영역만 남김
    private String extractJsonArray(String response) {
        if (!StringUtils.hasText(response)) {
            return "";
        }

        // think=false로도 thinking 출력이 남는 경우 </think> 이후 본문만 사용
        int thinkEnd = response.lastIndexOf("</think>");
        String body = thinkEnd >= 0 ? response.substring(thinkEnd + "</think>".length()) : response;

        int start = body.indexOf('[');
        if (start < 0) {
            return "";
        }

        int end = body.lastIndexOf(']');
        return end >= start ? body.substring(start, end + 1) : body.substring(start);
    }

    // 문자열(따옴표/이스케이프) 내부는 무시하고 중괄호 깊이만 추적해 최상위 { ... } 블록을 추출.
    // 쉼표 누락/중복, 마지막 항목의 '}' 누락처럼 항목 사이의 포맷 오류에는 영향받지 않음
    private List<String> splitTopLevelObjects(String arrayText) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        boolean inString = false;
        char quoteChar = 0;
        boolean escaped = false;
        int objectStart = -1;

        for (int i = 0; i < arrayText.length(); i++) {
            char c = arrayText.charAt(i);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == quoteChar) {
                    inString = false;
                }
                continue;
            }

            if (c == '"' || c == '\'') {
                inString = true;
                quoteChar = c;
                continue;
            }

            if (c == '{') {
                if (depth == 0) {
                    objectStart = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objectStart >= 0) {
                    objects.add(arrayText.substring(objectStart, i + 1));
                    objectStart = -1;
                }
            }
        }

        return objects;
    }

    private record ParsedRecommendation(
            Long id,
            boolean recommended,
            String recommendationReason
    ) {
    }
}
