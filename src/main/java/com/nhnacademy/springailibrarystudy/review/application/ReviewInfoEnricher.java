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

/**
 * 검색 결과에 리뷰 정보를 붙이는 컴포넌트 (Phase 2).
 *
 * [N+1 문제를 피하는 방법]
 * 도서마다 리뷰를 따로 조회하면 100권 → 100번 쿼리(N+1).
 * 여기서는 검색된 도서 id 들을 모아 한 번의 IN 쿼리(findAllById)로
 * 리뷰 통계를 통째로 가져와 Map 으로 만든 뒤 메모리에서 매칭한다.
 * → 리뷰 조회는 항상 딱 1번.
 *
 * 사용법: 검색 서비스가 BookSearchItemResponse 리스트를 만든 직후,
 *        반환 전에 enrich() 를 호출.
 */
@Component
@RequiredArgsConstructor
public class ReviewInfoEnricher {

    private final BookReviewSummaryRepository summaryRepository;

    public List<BookSearchItemResponse> enrich(List<BookSearchItemResponse> books) {
        if (books == null || books.isEmpty()) {
            return books;
        }

        // 1) 검색된 도서 id 수집
        List<Long> bookIds = books.stream()
                .map(BookSearchItemResponse::id)
                .toList();

        // 2) 리뷰 통계를 IN 쿼리 '한 번'으로 조회 → bookId 로 빠르게 찾도록 Map 화
        Map<Long, BookReviewSummary> summaryMap = summaryRepository.findAllById(bookIds).stream()
                .collect(Collectors.toMap(BookReviewSummary::getBookId, Function.identity()));

        // 3) 각 도서에 리뷰 정보 붙이기 (리뷰 없으면 원본 그대로)
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