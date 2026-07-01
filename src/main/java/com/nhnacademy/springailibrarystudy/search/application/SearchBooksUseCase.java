package com.nhnacademy.springailibrarystudy.search.application;

import com.nhnacademy.springailibrarystudy.search.domain.SearchType;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchBooksUseCase {

    private final SearchBooksByKeywordUseCase searchBooksByKeywordUseCase;
    private final SearchBooksByVectorUseCase searchBooksByVectorUseCase;
    private final SearchBooksHybridUseCase searchBooksHybridUseCase;

    public Page<BookSearchItemResponse> search(BookSearchRequest request, Pageable pageable) {
        return switch (resolveSearchType(request)) {
            case KEYWORD -> searchBooksByKeywordUseCase.search(request, pageable);
            case VECTOR -> searchBooksByVectorUseCase.search(request, pageable);
            case HYBRID -> searchBooksHybridUseCase.search(request, pageable);
            case RAG -> null;
        };
    }

    private SearchType resolveSearchType(BookSearchRequest request) {
        if (request.searchType() == null) {
            return SearchType.KEYWORD;
        }

        return request.searchType();
    }
}
