package com.nhnacademy.springailibrarystudy.search.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookSearchItemResponse(
        Long id,
        String volumeTitle,
        String title,
        String authorName,
        String publisherName,
        LocalDate publishedDate,
        BigDecimal price,
        String imageUrl
        // 나중에 score들 추가 가능
) {
}
