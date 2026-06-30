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

    public GenerateRagAnswerResult answer(GenerateRagAnswerCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        if (!StringUtils.hasText(command.question())) {
            return fallback("질문을 입력해주세요.", List.of(), command.recommendationTopK());
        }

        log.info("RAG 추천 시작: question='{}', candidateTopK={}, recommendationTopK={}",
                command.question(), command.candidateTopK(), command.recommendationTopK());

        List<RagBookCandidate> candidates =
                hybridBookCandidateSearcher.search(command.question(), command.candidateTopK());
        if (candidates.isEmpty()) {
            return fallback("질문과 관련된 도서를 찾지 못했습니다.", candidates, command.recommendationTopK());
        }

        String context = ragContextBuilder.build(candidates);
        Prompt prompt = ragPromptBuilder.build(command.question(), context, command.recommendationTopK());
        List<RagBookRecommendation> books = recommend(prompt, candidates, command.recommendationTopK());

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
                        .formatted(candidates.size(), books.size()),
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
