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
        Double rrfScore,
        // 리뷰 정보
        BigDecimal averageRating,
        Long reviewCount,
        String reviewSummary

) {
    public static BookSearchItemResponse ofKeyword(Long id, String isbn13, String volumeTitle, String title,
                                                   String authorName, String publisherName, LocalDate publishedDate,
                                                   BigDecimal price, String imageUrl, String description) {
        return new BookSearchItemResponse(id, isbn13, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, description, null, null, null, null, null);
    }
    public static BookSearchItemResponse ofVector(Long id, String isbn13, String volumeTitle, String title,
                                                  String authorName, String publisherName, LocalDate publishedDate,
                                                  BigDecimal price, String imageUrl, String description,
                                                  Double similarity) {
        return new BookSearchItemResponse(id, isbn13, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, description, similarity, null, null, null, null);
    }
    public static BookSearchItemResponse ofHybrid(Long id, String isbn13, String volumeTitle, String title,
                                                  String authorName, String publisherName, LocalDate publishedDate,
                                                  BigDecimal price, String imageUrl, String description,
                                                  Double similarity, Double rrfScore) {
        return new BookSearchItemResponse(id, isbn13, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, description, similarity, rrfScore, null, null, null);
    }

    public BookSearchItemResponse withReviewInfo(BigDecimal averageRating, Long reviewCount, String reviewSummary) {
        return new BookSearchItemResponse(id, isbn13, volumeTitle, title, authorName, publisherName, publishedDate, price, imageUrl, description, similarity, rrfScore, averageRating, reviewCount, reviewSummary);
    }

}
