package com.nhnacademy.springailibrarystudy.search.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// fixme: isbn, description 없음
public record BookSearchItemResponse(
        Long id,
        String volumeTitle,
        String title,
        String authorName,
        String publisherName,
        LocalDate publishedDate,
        BigDecimal price,
        String imageUrl,
        // 나중에 score들 추가 가능
        Double similarity,
        Double rrfScore
) {
    public BookSearchItemResponse(Long id, String volumeTitle, String title,
                                  String authorName, String publisherName, LocalDate publishedDate,
                                  BigDecimal price, String imageUrl) {
        this(id, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, null, null);
    }

    public BookSearchItemResponse(Long id, String volumeTitle, String title,
                                  String authorName, String publisherName, LocalDate publishedDate,
                                  BigDecimal price, String imageUrl, Double similarity) {
        this(id, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, similarity, null);
    }
}
