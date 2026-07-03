package com.nhnacademy.springailibrarystudy.review.application;

import com.nhnacademy.springailibrarystudy.review.domain.BookReviewSummary;
import com.nhnacademy.springailibrarystudy.review.infrastructure.BookReviewSummaryRepository;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class ReviewInfoEnricher {

    private final BookReviewSummaryRepository summaryRepository;

    public List<BookSearchItemResponse> enrich(List<BookSearchItemResponse> books) {
        if (books == null || books.isEmpty()) {
            return books;
        }

        List<Long> bookIds = books.stream()
                .map(BookSearchItemResponse::id)
                .toList();

        // 리뷰 통계를 IN 쿼리 '한 번'으로 조회 → bookId 로 빠르게 찾도록 Map 화
        Map<Long, BookReviewSummary> summaryMap = summaryRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(BookReviewSummary::getBookId, Function.identity()));

        // 각 도서에 리뷰 정보 붙이기 (리뷰 없으면 원본 그대로)
        return books.stream()
                .map(book -> {
                    BookReviewSummary summary = summaryMap.get(book.id());
                    if (summary == null) {
                        return book;  // 리뷰 통계가 없는 도서
                    }
                    return book.withReviewInfo(
                            summary.getAverageRating(),
                            summary.getReviewCount(),
                            summary.getReviewSummary()
                    );
                })
                .toList();
    }
}