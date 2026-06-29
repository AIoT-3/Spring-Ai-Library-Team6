package com.nhnacademy.springailibrarystudy.rag.application;

import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookQueryRepository;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerCommand;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerResult;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookRecommendation;
import com.nhnacademy.springailibrarystudy.search.application.SearchBooksUseCase;
import com.nhnacademy.springailibrarystudy.search.domain.SearchType;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GenerateRagAnswerUseCase {

    private static final String EMBEDDING_MODEL = "bge-m3";

    private final SearchBooksUseCase searchBooksUseCase;

    private final EmbeddingModel embeddingModel;
    private final BookQueryRepository bookQueryRepository;
    private final RagContextBuilder ragContextBuilder;
    private final RagPromptBuilder ragPromptBuilder;
    private final RagRecommendationGenerator recommendationGenerator;
    private final RagRecommendationFallbackBuilder fallbackBuilder;

    public GenerateRagAnswerResult answer(GenerateRagAnswerCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        if (!StringUtils.hasText(command.question())) {
            return fallback("질문을 입력해주세요.", List.of(), command.recommendationTopK());
        }

        if (command.searchType() != SearchType.VECTOR) {
            // 현재 RAG skeleton은 VECTOR 후보 검색만 고정한다.
            // HYBRID/KEYWORD RAG는 후보 DTO에 description/isbn을 포함하는 별도 조회가 준비된 뒤 연결해야함
            return fallback("현재 RAG 답변은 VECTOR 검색 기준으로만 생성할 수 있습니다.", List.of(), command.recommendationTopK());
        }

        List<RagBookCandidate> candidates = findCandidates(command);
        if (candidates.isEmpty()) {
            return fallback("질문과 관련된 도서를 찾지 못했습니다.", candidates, command.recommendationTopK());
        }

        String context = ragContextBuilder.build(candidates);
        Prompt prompt = ragPromptBuilder.build(command.question(), context, command.recommendationTopK());
        List<RagBookRecommendation> books = recommend(prompt, candidates, command.recommendationTopK());

        boolean fallback = books.isEmpty();
        if (fallback) {
            books = fallbackBuilder.build(candidates, command.recommendationTopK());
        }

        return new GenerateRagAnswerResult(
                UUID.randomUUID().toString(),
                fallback
                        ? "LLM 추천 생성에 실패하여 검색 결과 상위 %d권을 표시합니다.".formatted(books.size())
                        : "후보 도서 %d권 중 질문에 적합한 도서 %d권을 추천했습니다."
                                .formatted(candidates.size(), books.size()),
                books,
                false,
                fallback
        );
    }

    private List<RagBookCandidate> findCandidates(GenerateRagAnswerCommand command) {
        float[] queryVector = embeddingModel.embed(command.question());
        return bookQueryRepository.findRagCandidatesByVector(
                queryVector,
                EMBEDDING_MODEL,
                command.candidateTopK()
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
            return List.of();
        }
    }

    private GenerateRagAnswerResult fallback(
            String answer,
            List<RagBookCandidate> candidates,
            int recommendationTopK
    ) {
        return new GenerateRagAnswerResult(
                UUID.randomUUID().toString(),
                answer,
                fallbackBuilder.build(candidates, recommendationTopK),
                false,
                true
        );
    }
}
