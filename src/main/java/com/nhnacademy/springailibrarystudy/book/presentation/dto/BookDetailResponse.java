package com.nhnacademy.springailibrarystudy.book.presentation.dto;

import java.math.BigDecimal;

// 검색과 무관한 책의 상세 정보를 담는 DTO
public record BookDetailResponse(
        Long id,
        String isbn13,
        String volumeTitle,
        String title,
        String authorName,
        String publisherName,
        String publishedDate,
        BigDecimal price,
        String imageUrl,
        String description,
        String kdcCode
) {
}
