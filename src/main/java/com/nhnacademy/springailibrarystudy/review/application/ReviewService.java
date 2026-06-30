package com.nhnacademy.springailibrarystudy.review.application;

import com.nhnacademy.springailibrarystudy.review.domain.BookReview;
import com.nhnacademy.springailibrarystudy.review.domain.ReviewCreatedEvent;
import com.nhnacademy.springailibrarystudy.review.infrastructure.BookReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookReview createReview(BookReview review) {
        BookReview saved = bookReviewRepository.save(review);
        eventPublisher.publishEvent(new ReviewCreatedEvent(saved.getBook().getId()));
        return saved;
    }
}