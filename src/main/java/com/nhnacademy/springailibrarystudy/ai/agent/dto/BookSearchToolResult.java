package com.nhnacademy.springailibrarystudy.ai.agent.dto;

import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;

public record BookSearchToolResult (
        Long id,
        String isbn13,
        String title,
        String author,
        String publisher
) {
    public static BookSearchToolResult from(BookSearchItemResponse item) {
        return new BookSearchToolResult(
                item.id(), item.isbn13(), item.title(), item.authorName(), item.publisherName());
    }
}
