package com.nhnacademy.springailibrarystudy.review.application;

import com.nhnacademy.springailibrarystudy.review.domain.ReviewSummaryTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewSummaryQueueProcessor {

    private final ReviewAiSummaryService aiSummaryService;
    private final ReviewSummaryQueueService queueService;

    @RabbitListener(queues = "${rabbitmq.queue.review-summary}", concurrency = "3-5")
    public void processTask(ReviewSummaryTask task) {
        Long bookId = task.bookId();
        log.info("[요약 작업 수신] bookId={}", bookId);
        try {
            aiSummaryService.generateSummary(bookId);
        } catch (Exception e) {
            log.error("[요약 작업 실패] bookId={}", bookId, e);
        } finally {
            queueService.clearPending(bookId);
        }
    }
}