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
    public static BookSearchItemResponse ofKeyword(Long id, String isbn13, String volumeTitle, String title,
                                                   String authorName, String publisherName, LocalDate publishedDate,
                                                   BigDecimal price, String imageUrl, String description) {
        return new BookSearchItemResponse(id, isbn13, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, description, null, null);
    }
    public static BookSearchItemResponse ofVector(Long id, String isbn13, String volumeTitle, String title,
                                                  String authorName, String publisherName, LocalDate publishedDate,
                                                  BigDecimal price, String imageUrl, String description,
                                                  Double similarity) {
        return new BookSearchItemResponse(id, isbn13, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, description, similarity, null);
    }
    public static BookSearchItemResponse ofHybrid(Long id, String isbn13, String volumeTitle, String title,
                                                  String authorName, String publisherName, LocalDate publishedDate,
                                                  BigDecimal price, String imageUrl, String description,
                                                  Double similarity, Double rrfScore) {
        return new BookSearchItemResponse(id, isbn13, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, description, similarity, rrfScore);
    }

}
