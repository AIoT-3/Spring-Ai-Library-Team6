package com.nhnacademy.springailibrarystudy.review.application;

import com.nhnacademy.springailibrarystudy.review.domain.BookReviewSummary;
import com.nhnacademy.springailibrarystudy.review.domain.ReviewAiSummaryEvent;
import com.nhnacademy.springailibrarystudy.review.domain.ReviewCreatedEvent;
import com.nhnacademy.springailibrarystudy.review.domain.ReviewStatistics;
import com.nhnacademy.springailibrarystudy.review.infrastructure.BookReviewRepository;
import com.nhnacademy.springailibrarystudy.review.infrastructure.BookReviewSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewStatisticsEventListener {

    private final BookReviewRepository bookReviewRepository;          // 리뷰 한 건
    private final BookReviewSummaryRepository summaryRepository;      // 리뷰 요약
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReviewCreated(ReviewCreatedEvent event) {
        Long bookId = event.bookId();
        log.info("[통계 갱신 시작] bookId={}", bookId);

        ReviewStatistics stats = bookReviewRepository.aggregateByBookId(bookId);

        BookReviewSummary summary = summaryRepository.findById(bookId)
                .orElseGet(() -> BookReviewSummary.create(bookId));

        BigDecimal avg = (stats.averageRating() == null)
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(stats.averageRating()).setScale(2, RoundingMode.HALF_UP);

        summary.updateStatistics(
                stats.reviewCount(), avg,
                (int) stats.rating1Count(), (int) stats.rating2Count(),
                (int) stats.rating3Count(), (int) stats.rating4Count(),
                (int) stats.rating5Count(),
                stats.lastReviewedAt()
        );
        summaryRepository.save(summary);

        log.info("[통계 갱신 완료] bookId={}, 리뷰수={}, 평균={}", bookId, stats.reviewCount(), avg);

        eventPublisher.publishEvent(new ReviewAiSummaryEvent(bookId));
    }
}