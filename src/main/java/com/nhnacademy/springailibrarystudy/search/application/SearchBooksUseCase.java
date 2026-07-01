package com.nhnacademy.springailibrarystudy.search.application;

import com.nhnacademy.springailibrarystudy.review.application.ReviewInfoEnricher;
import com.nhnacademy.springailibrarystudy.search.domain.SearchType;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchBooksUseCase {

    private final SearchBooksByKeywordUseCase searchBooksByKeywordUseCase;
    private final SearchBooksByVectorUseCase searchBooksByVectorUseCase;
    private final SearchBooksHybridUseCase searchBooksHybridUseCase;
    private final ReviewInfoEnricher reviewInfoEnricher;

    public Page<BookSearchItemResponse> search(BookSearchRequest request, Pageable pageable) {
        Page<BookSearchItemResponse> result = switch (resolveSearchType(request)) {
            case KEYWORD -> searchBooksByKeywordUseCase.search(request, pageable);
            case VECTOR -> searchBooksByVectorUseCase.search(request, pageable);
            case HYBRID -> searchBooksHybridUseCase.search(request, pageable);
        };

        List<BookSearchItemResponse> enrichedContent = reviewInfoEnricher.enrich(result.getContent());
        return new PageImpl<>(enrichedContent, pageable, result.getTotalElements());
    }

    private SearchType resolveSearchType(BookSearchRequest request) {
        if (request.searchType() == null) {
            return SearchType.KEYWORD;
        }

        return request.searchType();
    }
}
