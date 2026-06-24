package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk;

import com.nhnacademy.springailibrarystudy.book.domain.Book;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookInsertRow(
        String isbn13,
        String volumeTitle,
        String title,
        String authorName,
        String publisherName,
        LocalDate publishedDate,
        BigDecimal price,
        String imageUrl,
        String description,
        String kdcCode
) {
    public static Book toBook(BookInsertRow row) {
        return Book.builder()
                .isbn13(row.isbn13())
                .volumeTitle(row.volumeTitle())
                .title(row.title())
                .authorName(row.authorName())
                .publisherName(row.publisherName())
                .publishedDate(row.publishedDate())
                .price(row.price())
                .imageUrl(row.imageUrl())
                .description(row.description())
                .kdcCode(row.kdcCode())
                .build();
    }
}
