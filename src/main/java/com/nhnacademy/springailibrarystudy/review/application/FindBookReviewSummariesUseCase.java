package com.nhnacademy.springailibrarystudy.review.application;

import java.util.List;

import com.nhnacademy.springailibrarystudy.review.application.dto.BookReviewSummary;
import org.springframework.stereotype.Service;

@Service
public class FindBookReviewSummariesUseCase {

    public List<BookReviewSummary> findByBookIds(List<Long> bookIds) {
        return List.of();
    }
}
