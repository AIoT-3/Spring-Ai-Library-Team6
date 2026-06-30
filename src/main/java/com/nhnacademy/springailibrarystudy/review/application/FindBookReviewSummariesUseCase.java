package com.nhnacademy.springailibrarystudy.review.application;

import java.util.List;

import com.nhnacademy.springailibrarystudy.review.application.dto.BookReviewSummaryResponse;
import org.springframework.stereotype.Service;

@Service
public class FindBookReviewSummariesUseCase {

    public List<BookReviewSummaryResponse> findByBookIds(List<Long> bookIds) {
        return List.of();
    }
}
