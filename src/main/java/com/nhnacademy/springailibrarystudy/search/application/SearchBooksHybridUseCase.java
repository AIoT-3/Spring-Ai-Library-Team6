package com.nhnacademy.springailibrarystudy.search.application;

import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchBooksHybridUseCase {

    private final SearchBooksByKeywordUseCase keywordSearch;
    private final SearchBooksByVectorUseCase vectorSearch;

    private final RrfService rrfService;

    public Page<BookSearchItemResponse> search(BookSearchRequest request, Pageable pageable) {
        log.info("하이브리드 검색 시작: query='{}', page={}, size={}",
                request.query(), pageable.getPageNumber(), pageable.getPageSize());

        List<BookSearchItemResponse> keywordSearchResponse = keywordSearch.search(request, pageable).getContent();
        List<BookSearchItemResponse> vectorSearchResponse = vectorSearch.search(request, pageable).getContent();

        List<BookSearchItemResponse> fusedSearchResponse = rrfService.fuse(
                keywordSearchResponse,
                vectorSearchResponse
        );

        log.info("하이브리드 검색 융합 완료: keyword={}건, vector={}건, fused={}건",
                keywordSearchResponse.size(), vectorSearchResponse.size(), fusedSearchResponse.size());
        if (log.isDebugEnabled()) {
            fusedSearchResponse.forEach(book -> log.debug(
                    "  융합 결과 id={}, title='{}', similarity={}, rrfScore={}",
                    book.id(), book.title(), book.similarity(), book.rrfScore()));
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), fusedSearchResponse.size());

        if (start > fusedSearchResponse.size()) {
            return new PageImpl<>(List.of(), pageable, fusedSearchResponse.size());
        }

        List<BookSearchItemResponse> pageList = fusedSearchResponse.subList(start, end);

        return new PageImpl<>(pageList, pageable, fusedSearchResponse.size());
    }
}
