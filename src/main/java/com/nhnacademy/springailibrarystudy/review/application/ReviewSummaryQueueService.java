package com.nhnacademy.springailibrarystudy.review.application;

import com.nhnacademy.springailibrarystudy.review.config.RabbitMQConfig;
import com.nhnacademy.springailibrarystudy.review.domain.ReviewSummaryTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewSummaryQueueService {

    private final RabbitTemplate rabbitTemplate;

    private static final long DEDUP_WINDOW_MS = 5000;
    private final ConcurrentHashMap<Long, Long> pendingBooks = new ConcurrentHashMap<>();

    public boolean enqueue(Long bookId) {
        if (!shouldEnqueue(bookId)) {
            return false;
        }
        pendingBooks.put(bookId, System.currentTimeMillis());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                ReviewSummaryTask.of(bookId)
        );
        return true;
    }

    private boolean shouldEnqueue(Long bookId) {
        Long last = pendingBooks.get(bookId);
        long now = System.currentTimeMillis();
        return (last == null) || (now - last) > DEDUP_WINDOW_MS;
    }

    public void clearPending(Long bookId) {
        pendingBooks.remove(bookId);
    }
}