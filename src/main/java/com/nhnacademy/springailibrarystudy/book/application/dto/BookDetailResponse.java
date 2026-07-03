package com.nhnacademy.springailibrarystudy.book.application.dto;

import com.nhnacademy.springailibrarystudy.review.domain.BookReview;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookDetailResponse(
        Long id,
        String isbn13,
        String title,
        String authorName,
        String publisherName,
        LocalDate publishedDate,
        BigDecimal price,
        String imageUrl,
        String description,

        // 리뷰 통계
        BigDecimal averageRating,
        Long reviewCount,
        String reviewSummary,

        // 리뷰 목록
        List<ReviewItem> reviews
) {

    /** 리뷰 한 건을 화면에 보여줄 형태로 담은 record */
    public record ReviewItem(
            Long id,
            Integer rating,
            String content,
            String author,
            LocalDateTime createdAt
    ) {
        public static ReviewItem from(BookReview review) {
            return new ReviewItem(
                    review.getId(),
                    review.getRating(),
                    review.getContent(),
                    review.getAuthor(),
                    review.getCreatedAt()
            );
        }
    }
}