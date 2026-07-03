package com.nhnacademy.springailibrarystudy.review.application;

import com.nhnacademy.springailibrarystudy.review.domain.BookReview;
import com.nhnacademy.springailibrarystudy.review.domain.BookReviewSummary;
import com.nhnacademy.springailibrarystudy.review.infrastructure.BookReviewRepository;
import com.nhnacademy.springailibrarystudy.review.infrastructure.BookReviewSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAiSummaryService {

    private final BookReviewRepository bookReviewRepository;      // 리뷰 한 건
    private final BookReviewSummaryRepository summaryRepository;  // 리뷰 요약
    private final ReviewSummarizer summarizer;

    private static final double REBUILD_RATIO_THRESHOLD = 0.3; // 30%

    @Transactional
    public void generateSummary(Long bookId) {
        BookReviewSummary summary = summaryRepository.findById(bookId).orElse(null);
        if (summary == null) {
            log.debug("[요약 스킵] 통계 없음 bookId={}", bookId);
            return;
        }
        if (!summary.shouldGenerateSummary()) {   // Dirty Flag 3조건
            log.debug("[요약 스킵] 조건 미충족 bookId={}", bookId);
            return;
        }

        // 중복 실행 방지 잠금
        summary.markAsGenerating();
        summaryRepository.saveAndFlush(summary);

        try {
            List<BookReview> newReviews =
                    bookReviewRepository.findNewReviewsAfterId(bookId, summary.getLastSummarizedCount());

            if (newReviews.isEmpty()) {
                summary.finishGenerating();
                summaryRepository.save(summary);
                return;
            }

            boolean fullRebuild = shouldFullRebuild(summary, newReviews.size());

            String resultSummary;
            Long lastSummarizedReviewId;

            if (fullRebuild) {
                log.info("[전체 재요약] bookId={} (새 리뷰 {}개 / 전체 {}개)",
                        bookId, newReviews.size(), summary.getReviewCount());
                List<BookReview> all = bookReviewRepository.findAllByBookId(bookId);
                resultSummary = summarizer.summarizeAll(all);
                lastSummarizedReviewId = all.get(all.size() - 1).getId();
            } else {
                log.info("[누적 요약] bookId={} (새 리뷰 {}개 / 전체 {}개)",
                        bookId, newReviews.size(), summary.getReviewCount());
                resultSummary = summarizer.summarizeIncremental(summary.getReviewSummary(), newReviews);
                lastSummarizedReviewId = newReviews.get(newReviews.size() - 1).getId();
            }

            summary.updateSummary(resultSummary, lastSummarizedReviewId);  // 저장 + dirty/generating 해제
            summaryRepository.save(summary);
            log.info("[요약 완료] bookId={}", bookId);

        } catch (Exception e) {
            summary.finishGenerating();
            summaryRepository.save(summary);
            throw e;
        }
    }

    /**
     * 다음 중 하나라도 해당하면 전체 재요약:
     * 1) 기존 요약이 아직 없음 (처음 요약하는 경우)
     * 2) 새 리뷰 비율이 임계값(기본 30%) 이상
     *    예) 전체 100개 중 새 리뷰 35개 → 35% → 전체 재요약
     */
    private boolean shouldFullRebuild(BookReviewSummary summary, int newReviewCount) {
        if (summary.getReviewSummary() == null || summary.getReviewSummary().isBlank()) {
            return true;
        }

        long totalReviewCount = summary.getReviewCount();
        if (totalReviewCount <= 0) {
            return true; // 방어 코드: 분모가 0 or 음수면 안전하게 전체 재요약
        }

        double newRatio = (double) newReviewCount / totalReviewCount;
        return newRatio >= REBUILD_RATIO_THRESHOLD;
    }
}