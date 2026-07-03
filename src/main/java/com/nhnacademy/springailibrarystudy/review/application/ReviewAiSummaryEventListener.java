package com.nhnacademy.springailibrarystudy.review.application;

import com.nhnacademy.springailibrarystudy.review.domain.ReviewAiSummaryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewAiSummaryEventListener {

    private final ReviewSummaryQueueService queueService;

    @Async
    @EventListener
    public void handleReviewAiSummary(ReviewAiSummaryEvent event) {
        boolean enqueued = queueService.enqueue(event.bookId());
        if (enqueued) {
            log.info("[큐 적재] bookId={}", event.bookId());
        } else {
            log.debug("[큐 적재 생략-중복] bookId={}", event.bookId());
        }
    }
}