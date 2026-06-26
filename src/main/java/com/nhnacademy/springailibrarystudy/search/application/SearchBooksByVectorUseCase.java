package com.nhnacademy.springailibrarystudy.search.application;

import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookQueryRepository;
import com.nhnacademy.springailibrarystudy.search.domain.SearchType;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchBooksByVectorUseCase {

    private final BookQueryRepository bookQueryRepository;
    private final EmbeddingModel embeddingModel;

    public Page<BookSearchItemResponse> search(BookSearchRequest request, Pageable pageable) {
        float[] vector = embeddingModel.embed(request.query());

        BookSearchRequest vectorRequest = new BookSearchRequest(
                request.query(),
                request.isbn(),
                request.kdcCode(),
                SearchType.VECTOR,
                vector
        );

        return bookQueryRepository.vectorSearch(pageable, vectorRequest);
    }
}
