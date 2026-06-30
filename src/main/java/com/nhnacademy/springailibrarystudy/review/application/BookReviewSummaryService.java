package com.nhnacademy.springailibrarystudy.review.application;

import com.nhnacademy.springailibrarystudy.review.application.dto.BookReviewSummaryResponse;
import com.nhnacademy.springailibrarystudy.review.infrastructure.BookReviewSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookReviewSummaryService {

    private final BookReviewSummaryRepository summaryRepository;

    public BookReviewSummaryResponse getSummary(Long bookId) {
        return summaryRepository.findById(bookId)
                .map(s -> new BookReviewSummaryResponse(
                        s.getBookId()
                        ,(s.getReviewSummary() != null && !s.getReviewSummary().isBlank())
                                ? s.getReviewSummary() : "아직 리뷰를 분석 중입니다. 잠시 후 다시 확인해주세요."
                        ,s.getReviewCount().intValue()
                        ,s.getAverageRating() != null ? s.getAverageRating().doubleValue() : null
                ))
                .orElse(new BookReviewSummaryResponse(bookId, "아직 리뷰가 없습니다.", 0, null));
    }
}