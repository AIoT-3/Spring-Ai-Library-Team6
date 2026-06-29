package com.nhnacademy.springailibrarystudy.search.application;

import com.nhnacademy.springailibrarystudy.search.domain.SearchType;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
class VectorSearchTest {

    @Autowired
    SearchBooksByVectorUseCase searchBooksByVectorUseCase;

    @Test
    void vectorSearch() {
        BookSearchRequest request = new BookSearchRequest(
                "스프링부트",
                null,
                null,
                SearchType.VECTOR,
                null
        );

        Page<BookSearchItemResponse> result = searchBooksByVectorUseCase.search(
                request,
                PageRequest.of(0, 20)
        );

        result.getContent().forEach(book ->
                System.out.println("id: " + book.id() + ",제목: " + book.title() + ", 유사도: " + book.similarity() + ", description: " + book.description())
        );
    }
}
