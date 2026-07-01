package com.nhnacademy.springailibrarystudy.rag.application;

import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerCommand;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerResult;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookRecommendation;

import java.util.List;
import java.util.Objects;
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

    // RRF 점수 기반 필터링에 사용할 수치
    private static final Double SCORE_THRESHOLD = 0.015;
    // AI에게 전달할 최대 도서 수
    private static final int FALLBACK_CANDIDATES = 10;

    public GenerateRagAnswerResult answer(GenerateRagAnswerCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        if (!StringUtils.hasText(command.question())) {
            return fallback("질문을 입력해주세요.", List.of(), command.recommendationTopK());
        }

        log.info("RAG 추천 시작: question='{}', candidateTopK={}, recommendationTopK={}",
                command.question(), command.candidateTopK(), command.recommendationTopK());

        // Rag 후보들 검색
        List<RagBookCandidate> candidates = hybridBookCandidateSearcher.search(
                command.question(),
                command.candidateTopK(),
                command.userKey()
        );
        if (candidates.isEmpty()) {
            return fallback("질문과 관련된 도서를 찾지 못했습니다.", candidates, command.recommendationTopK());
        }

        // FIXME: 개인화 reranking이 적용된 후보들에 대해 rrf 점수 기반 필터링은 의미가 약해짐 (개인화로 보정된 후보들이 잘릴 수 있음)
        // FIXME: RRF 점수 기반 필터링을 적용하고자 한다면, 위 rag 후보를 가져오는 과정에서 개인화 reranking의 여부에 따라 선택적으로 적용하는 것 고려 (지금은 주석처리 돼있어서 그냥 둠)
        //RRF 점수 기반 팔티렁을 통해 얻어낸 도서 {DEFAULT_BATCH_SIZE}권에서
        //RRF 점수가 기준치를 넘어가는 도서들에 대해, AI에게 전달할 도서 수 만큼만 걸러냄
        List<RagBookCandidate> filteredCandidates = candidates.stream()
                .filter(book -> book.rrfScore() != null) // && book.rrfScore() >= SCORE_THRESHOLD)
                .limit(FALLBACK_CANDIDATES)
                .toList();
        // RRF 점수 기반 필터링 대신, RRF 점수 기준 내림차순으로 정렬된 도서들 중 상위 10권만 필터링

        for (RagBookCandidate ragBookCandidate : filteredCandidates) {
            // FIXME: 로깅 레벨을 info에서 debug로 변경하는 것 고려
            log.info("id: {}, title: {}, rrfScore: {}", ragBookCandidate.id(), ragBookCandidate.title(), ragBookCandidate.rrfScore());
        }

        // 책 추천 생성
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

        return result(
                "후보 도서 %d권 중 질문에 적합한 도서 %d권을 추천했습니다."
                        .formatted(filteredCandidates.size(), books.size()),
                books,
                false
        );
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
}
