package com.nhnacademy.springailibrarystudy.cache.application;

import com.nhnacademy.springailibrarystudy.cache.infrastructure.BookSearchCacheQueryRepository;
import com.nhnacademy.springailibrarystudy.cache.infrastructure.CachedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticCacheService {

    private final BookSearchCacheQueryRepository cacheQueryRepository;
    private final EmbeddingModel embeddingModel;

    /** 코사인 유사도 임계값 (이 값 이상이면 '같은 질문'으로 취급) */
    private static final double SIMILARITY_THRESHOLD = 0.95;

    /** 기본 TTL: 30분 */
    private static final int DEFAULT_TTL_SECONDS = 1800;

    public Optional<String> get(String query) {
        float[] queryEmbedding = embeddingModel.embed(query);

        Optional<CachedResult> match = cacheQueryRepository.findMostSimilar(queryEmbedding, SIMILARITY_THRESHOLD);

        if (match.isPresent()) {
            CachedResult cached = match.get();
            log.info("시맨틱 캐시 적중: query='{}', 유사도={}", query, String.format("%.4f", cached.similarity()));
            cacheQueryRepository.touch(cached.id());
            return Optional.of(cached.result());
        }

        log.info("시맨틱 캐시 미적중: query='{}'", query);
        return Optional.empty();
    }

    public void put(String query, String result) {
        float[] embedding = embeddingModel.embed(query);
        cacheQueryRepository.save(query, embedding, result, DEFAULT_TTL_SECONDS);
        log.info("시맨틱 캐시 저장: query='{}'", query);
    }
}