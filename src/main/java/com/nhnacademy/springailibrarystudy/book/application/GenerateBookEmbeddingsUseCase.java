package com.nhnacademy.springailibrarystudy.book.application;

import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingGenerationOptions;
import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingGenerationResult;
import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingTarget;
import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookEmbeddingRepository;
import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Service
@ConditionalOnProperty(
        prefix = "book.embedding.generation",
        name = "enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class GenerateBookEmbeddingsUseCase {

    private final EmbeddingModel embeddingModel;
    private final BookEmbeddingSourceTextBuilder sourceTextBuilder;
    private final BookEmbeddingRepository bookEmbeddingRepository;

    public BookEmbeddingGenerationResult generate(BookEmbeddingGenerationOptions options) {
        StopWatch stopWatch = new StopWatch();
        long lastBookId = 0L;
        int targetCount = 0;
        int insertedCount = 0;

        stopWatch.start();
        while (options.rowLimit() == 0 || targetCount < options.rowLimit()) {
            // book id 기준으로 후보 도서를 batchSize만큼 조회
            List<BookEmbeddingTarget> candidates = bookEmbeddingRepository.findCandidateTargetsAfter(
                    lastBookId,
                    options.batchSize()
            );
            if (candidates.isEmpty()) {
                break;
            }
            lastBookId = candidates.getLast().bookId();

            // 현재 전처리 규칙으로 source text와 hash를 만든 뒤, 이미 생성된 임베딩인지 확인
            List<String> candidateSourceTexts = candidates.stream()
                    .map(sourceTextBuilder::build)
                    .toList();
            List<String> candidateSourceTextHashes = candidateSourceTexts.stream()
                    .map(sourceTextBuilder::hash)
                    .toList();
            Map<Long, String> sourceTextHashByBookId = toSourceTextHashByBookId(
                    candidates,
                    candidateSourceTextHashes
            );
            Set<Long> existingBookIds = bookEmbeddingRepository.findExistingBookIds(
                    options.embeddingModel(),
                    sourceTextHashByBookId
            );

            // 같은 book, model, source hash 조합이 있으면 재실행 대상에서 제외
            List<BookEmbeddingTarget> targets = new ArrayList<>();
            List<String> sourceTexts = new ArrayList<>();
            List<String> sourceTextHashes = new ArrayList<>();
            int remainingCount = remainingCount(options, targetCount);
            for (int i = 0; i < candidates.size() && targets.size() < remainingCount; i++) {
                BookEmbeddingTarget candidate = candidates.get(i);
                if (!existingBookIds.contains(candidate.bookId())) {
                    targets.add(candidate);
                    sourceTexts.add(candidateSourceTexts.get(i));
                    sourceTextHashes.add(candidateSourceTextHashes.get(i));
                }
            }
            if (targets.isEmpty()) {
                continue;
            }

            // 외부 임베딩 API 호출은 실제로 저장이 필요한 대상에 대해서만 수행
            List<float[]> embeddings = embeddingModel.embed(sourceTexts);
            if (embeddings.size() != targets.size()) {
                throw new BusinessException(ErrorCode.BOOK_EMBEDDING_GENERATION_FAILED);
            }

            // 임베딩 결과를 DB에 Batch Insert
            insertedCount += bookEmbeddingRepository.batchInsert(
                    options.embeddingModel(),
                    targets,
                    sourceTexts,
                    sourceTextHashes,
                    embeddings
            );
            targetCount += targets.size();
        }
        stopWatch.stop();

        return new BookEmbeddingGenerationResult(
                options.embeddingModel(),
                options.batchSize(),
                options.rowLimit(),
                targetCount,
                insertedCount,
                stopWatch.getTotalTimeMillis()
        );
    }

    private int remainingCount(BookEmbeddingGenerationOptions options, int targetCount) {
        if (options.rowLimit() == 0) {
            return Integer.MAX_VALUE;
        }

        return options.rowLimit() - targetCount;
    }

    private Map<Long, String> toSourceTextHashByBookId(
            List<BookEmbeddingTarget> targets,
            List<String> sourceTextHashes
    ) {
        Map<Long, String> sourceTextHashByBookId = new LinkedHashMap<>();
        for (int i = 0; i < targets.size(); i++) {
            sourceTextHashByBookId.put(targets.get(i).bookId(), sourceTextHashes.get(i));
        }
        return sourceTextHashByBookId;
    }

}
