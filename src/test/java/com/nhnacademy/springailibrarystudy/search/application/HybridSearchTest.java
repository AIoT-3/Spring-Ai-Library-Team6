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
public class HybridSearchTest {

    @Autowired
    SearchBooksHybridUseCase searchBooksHybridUseCase;

    @Test
    void hybridSearch() {
        BookSearchRequest request = new BookSearchRequest(
                "자바 프로그래밍",
                null,
                null,
                SearchType.HYBRID,
                null
        );

        Page<BookSearchItemResponse> responses = searchBooksHybridUseCase.search(
                request,
                PageRequest.of(0, 10)
        );

        responses.getContent().forEach(book ->
                System.out.println("id: " + book.id() + ",제목: " + book.title() + ", rrf: " + book.rrfScore() + ", description: " + book.description())
        );

    }
}
