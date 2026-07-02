package com.nhnacademy.springailibrarystudy.rag.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.springailibrarystudy.cache.application.SemanticCacheService;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerCommand;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerResult;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookRecommendation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.nhnacademy.springailibrarystudy.rag.infrastructure.HybridBookCandidateSearcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchBooksRagUseCase {

    private final HybridBookCandidateSearcher hybridBookCandidateSearcher;
    private final RagContextBuilder ragContextBuilder;
    private final RagPromptBuilder ragPromptBuilder;
    private final RagRecommendationGenerator recommendationGenerator;
    private final RagRecommendationFallbackBuilder fallbackBuilder;
    // 캐싱용
    private final SemanticCacheService semanticCacheService;
    private final ObjectMapper objectMapper;

    // RRF 점수 기반 필터링에 사용할 수치
    private static final Double SCORE_THRESHOLD = 0.015;
    // AI에게 전달할 최대 도서 수
    private static final int FALLBACK_CANDIDATES = 10;

    public GenerateRagAnswerResult answer(GenerateRagAnswerCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        if (!StringUtils.hasText(command.question())) {
            return fallback("질문을 입력해주세요.", List.of(), command.recommendationTopK());
        }

        // 캐시 조회
        Optional<String> cachedJson = semanticCacheService.get(command.question());
        if (cachedJson.isPresent()) {
            GenerateRagAnswerResult cached = deserialize(cachedJson.get());
            if (cached != null) {
                log.info("[RAG] 캐시 적중으로 즉시 반환: question='{}'", command.question());
                return cached;
            }
        }

        log.info("RAG 추천 시작: question='{}', candidateTopK={}, recommendationTopK={}",
                command.question(), command.candidateTopK(), command.recommendationTopK());

        List<RagBookCandidate> candidates =
                hybridBookCandidateSearcher.search(command.question(), command.candidateTopK());
        if (candidates.isEmpty()) {
            return fallback("질문과 관련된 도서를 찾지 못했습니다.", candidates, command.recommendationTopK());
        }

        //RRF 점수 기반 팔티렁을 통해 얻어낸 도서 {DEFAULT_BATCH_SIZE}권에서
        //RRF 점수가 기준치를 넘어가는 도서들에 대해, AI에게 전달할 도서 수 만큼만 걸러냄
        List<RagBookCandidate> filteredCandidates = candidates.stream()
                .filter(book -> book.rrfScore() != null) // && book.rrfScore() >= SCORE_THRESHOLD)
                .limit(FALLBACK_CANDIDATES)
                .toList();
        // RRF 점수 기반 필터링 대신, RRF 점수 기준 내림차순으로 정렬된 도서들 중 상위 10권만 필터링

        for (RagBookCandidate ragBookCandidate : filteredCandidates) {
            log.info("id: {}, title: {}, rrfScore: {}", ragBookCandidate.id(), ragBookCandidate.title(), ragBookCandidate.rrfScore());
        }

        String context = ragContextBuilder.build(filteredCandidates);
        Prompt prompt = ragPromptBuilder.build(command.question(), context, command.recommendationTopK());
        List<RagBookRecommendation> books = recommend(prompt, filteredCandidates, command.recommendationTopK());

        if (books.isEmpty()) {
            List<RagBookRecommendation> fallbackBooks =
                    fallbackBuilder.build(candidates, command.recommendationTopK());
            return result(
                    "LLM 추천 생성에 실패하여 검색 결과 상위 %d권을 표시합니다.".formatted(fallbackBooks.size()),
                    fallbackBooks,
                    true
            );
        }

        GenerateRagAnswerResult finalResult = result(
                "후보 도서 %d권 중 질문에 적합한 도서 %d권을 추천했습니다."
                        .formatted(filteredCandidates.size(), books.size()),
                books,
                false
        );

        // 성공(fallback=false) 결과만 캐시에 저장
        cacheIfSuccessful(command.question(), finalResult);

        return finalResult;
    }

    private List<RagBookRecommendation> recommend(
            Prompt prompt,
            List<RagBookCandidate> candidates,
            int recommendationTopK
    ) {
        try {
            return recommendationGenerator.generate(prompt, candidates, recommendationTopK);
        } catch (Exception e) {
            log.warn("LLM 추천 생성 실패, 폴백으로 전환합니다.", e);
            return List.of();
        }
    }

    private GenerateRagAnswerResult fallback(
            String answer,
            List<RagBookCandidate> candidates,
            int recommendationTopK
    ) {
        return result(answer, fallbackBuilder.build(candidates, recommendationTopK), true);
    }

    private GenerateRagAnswerResult result(
            String answer,
            List<RagBookRecommendation> books,
            boolean fallback
    ) {
        return new GenerateRagAnswerResult(UUID.randomUUID().toString(), answer, books, false, fallback);
    }

    // 캐시 저장 헬퍼들
    private void cacheIfSuccessful(String question, GenerateRagAnswerResult result) {
        if (result.fallback()) {
            return;
        }
        String json = serialize(result);
        if (json != null) {
            semanticCacheService.put(question, json);
        }
    }

    private String serialize(GenerateRagAnswerResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            log.warn("[RAG] 결과 직렬화 실패(캐시 저장 생략): {}", e.toString());
            return null;
        }
    }

    private GenerateRagAnswerResult deserialize(String json) {
        try {
            return objectMapper.readValue(json, GenerateRagAnswerResult.class);
        } catch (Exception e) {
            log.warn("[RAG] 캐시 역직렬화 실패(정상 경로로 진행): {}", e.toString());
            return null;
        }
    }
}
