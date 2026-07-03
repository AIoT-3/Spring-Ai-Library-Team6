package com.nhnacademy.springailibrarystudy.book.application;

import com.nhnacademy.springailibrarystudy.book.application.dto.BookDetailResponse;
import com.nhnacademy.springailibrarystudy.book.domain.Book;
import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookRepository;
import com.nhnacademy.springailibrarystudy.review.domain.BookReview;
import com.nhnacademy.springailibrarystudy.review.domain.BookReviewSummary;
import com.nhnacademy.springailibrarystudy.review.infrastructure.BookReviewRepository;
import com.nhnacademy.springailibrarystudy.review.infrastructure.BookReviewSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookDetailService {

    private final BookRepository bookRepository;
    private final BookReviewSummaryRepository reviewSummaryRepository;
    private final BookReviewRepository bookReviewRepository;

    public BookDetailResponse getDetail(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("책을 찾을 수 없습니다. id=" + bookId));

        // 리뷰 통계/요약
        BookReviewSummary summary = reviewSummaryRepository.findById(bookId).orElse(null);

        // 리뷰 목록
        List<BookReview> reviews = bookReviewRepository.findAllByBookId(bookId);
        List<BookDetailResponse.ReviewItem> reviewItems = reviews.stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId())) // 최신순(id 내림차순)
                .map(BookDetailResponse.ReviewItem::from)
                .toList();

        return new BookDetailResponse(
                book.getId(),
                book.getIsbn13(),
                book.getTitle(),
                book.getAuthorName(),
                book.getPublisherName(),
                book.getPublishedDate(),
                book.getPrice(),
                book.getImageUrl(),
                book.getDescription(),
                summary != null ? summary.getAverageRating() : null,
                summary != null ? summary.getReviewCount() : null,
                summary != null ? summary.getReviewSummary() : null,
                reviewItems
        );
    }
}