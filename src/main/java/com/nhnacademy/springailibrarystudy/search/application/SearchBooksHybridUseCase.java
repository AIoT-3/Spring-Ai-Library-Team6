package com.nhnacademy.springailibrarystudy.search.application;

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
public class SearchBooksHybridUseCase {

    private final SearchBooksByKeywordUseCase keywordSearch;
    private final SearchBooksByVectorUseCase vectorSearch;

    private final RrfService rrfService;

    public Page<BookSearchItemResponse> search(BookSearchRequest request, Pageable pageable) {
        List<BookSearchItemResponse> keywordSearchResponse = keywordSearch.search(request, pageable).getContent();
        List<BookSearchItemResponse> vectorSearchResponse = vectorSearch.search(request, pageable).getContent();

        List<BookSearchItemResponse> fusedSearchResponse = rrfService.fuse(
                keywordSearchResponse,
                vectorSearchResponse
        );

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), fusedSearchResponse.size());

        if (start > fusedSearchResponse.size()) {
            return new PageImpl<>(List.of(), pageable, fusedSearchResponse.size());
        }

        List<BookSearchItemResponse> pageList = fusedSearchResponse.subList(start, end);

        return new PageImpl<>(pageList, pageable, fusedSearchResponse.size());
    }
}
