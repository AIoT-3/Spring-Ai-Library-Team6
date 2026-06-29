package com.nhnacademy.springailibrarystudy.rag.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookRecommendation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
        String response = chatModel.call(prompt).getResult().getOutput().getText();
        List<ParsedRecommendation> parsedRecommendations = parse(response);
        Map<Long, RagBookCandidate> candidateById = toCandidateMap(candidates);

        return parsedRecommendations.stream()
                .filter(parsed -> candidateById.containsKey(parsed.id()))
                .limit(recommendationTopK)
                .map(parsed -> RagBookRecommendation.of(
                        candidateById.get(parsed.id()),
                        parsed.recommendationReason()
                ))
                .toList();
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
            JsonNode root = objectMapper.readTree(extractJsonArray(response));
            if (!root.isArray()) {
                return List.of();
            }

            return root.findValuesAsText("id").stream()
                    .map(Long::valueOf)
                    .map(id -> new ParsedRecommendation(id, findReason(root, id)))
                    .filter(parsed -> StringUtils.hasText(parsed.recommendationReason()))
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private String findReason(JsonNode root, Long id) {
        for (JsonNode item : root) {
            JsonNode idNode = item.get("id");
            if (idNode != null && idNode.asLong() == id) {
                JsonNode reasonNode = item.get("recommendationReason");
                return reasonNode == null ? "" : reasonNode.asText();
            }
        }
        return "";
    }

    private String extractJsonArray(String response) {
        if (!StringUtils.hasText(response)) {
            return "[]";
        }

        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start < 0 || end < start) {
            return "[]";
        }
        return response.substring(start, end + 1);
    }

    private record ParsedRecommendation(
            Long id,
            String recommendationReason
    ) {
    }
}
