package com.nhnacademy.springailibrarystudy.search.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookSearchItemResponse(
        Long id,
        String isbn13,
        String volumeTitle,
        String title,
        String authorName,
        String publisherName,
        LocalDate publishedDate,
        BigDecimal price,
        String imageUrl,
        String description,
        // 나중에 score들 추가 가능
        Double similarity,
        Double rrfScore

) {
    public BookSearchItemResponse(Long id, String volumeTitle, String title,
                                  String authorName, String publisherName, LocalDate publishedDate,
                                  BigDecimal price, String imageUrl) {
        this(id, null, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, null, null, null);
    }

    public BookSearchItemResponse(Long id, String isbn13, String volumeTitle, String title,
                                  String authorName, String publisherName, LocalDate publishedDate,
                                  BigDecimal price, String imageUrl, String description, Double similarity) {
        this(id, isbn13, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, description, similarity, null);
    }

    public BookSearchItemResponse(Long id, String volumeTitle, String title,
                                  String authorName, String publisherName, LocalDate publishedDate,
                                  BigDecimal price, String imageUrl, Double similarity) {
        this(id, null, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, null, similarity, null);
    }
}
