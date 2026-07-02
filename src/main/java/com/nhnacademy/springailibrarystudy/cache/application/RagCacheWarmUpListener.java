package com.nhnacademy.springailibrarystudy.cache.application;

import com.nhnacademy.springailibrarystudy.rag.application.SearchBooksRagUseCase;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RagCacheWarmUpListener {

    private final SearchBooksRagUseCase searchBooksRagUseCase;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        log.info("[캐시 웜업 시작] 인기 질문 {}개", PopularQueries.values().length);
        int success = 0;

        for (PopularQueries popular : PopularQueries.values()) {
            try {
                searchBooksRagUseCase.answer(GenerateRagAnswerCommand.of(popular.getQuery()));
                success++;
                log.info("[캐시 웜업] '{}' 완료 ({}/{})", popular.getQuery(), success, PopularQueries.values().length);
            } catch (Exception e) {
                log.error("[캐시 웜업 실패] '{}': {}", popular.getQuery(), e.toString());
            }
        }
        log.info("[캐시 웜업 종료] {}건 캐시 적재", success);
    }
}