package com.nhnacademy.springailibrarystudy.search.application;

import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchBooksHybridUseCase {

    private final SearchBooksByKeywordUseCase keywordSearch;
    private final SearchBooksByVectorUseCase vectorSearch;
    private final RrfService rrfService;

    private final Executor taskExecutor;

    private static final int DEFAULT_BATCH_SIZE = 100;

    public Page<BookSearchItemResponse> search(BookSearchRequest request, Pageable pageable) {
        log.info("하이브리드 검색 시작: query='{}', page={}, size={}",
                request.query(), pageable.getPageNumber(), pageable.getPageSize());

        // CompletableFuture를 활용한 병렬 처리 적용

        // 병렬실행 1 : 키워드 검색
        CompletableFuture<List<BookSearchItemResponse>> keywordSearchFuture = CompletableFuture.supplyAsync(() -> {
            var keywordPage = keywordSearch.search(request, PageRequest.of(0, DEFAULT_BATCH_SIZE));
            return (keywordPage != null && keywordPage.getContent() != null)
                    ? keywordPage.getContent()
                    :List.of();
        }, taskExecutor);

        // 병렬실행 2 : 벡터 검색
        CompletableFuture<List<BookSearchItemResponse>> vectorSearchFuture = CompletableFuture.supplyAsync(() -> {
            var vectorPage = vectorSearch.search(request, PageRequest.of(0, DEFAULT_BATCH_SIZE));
            return (vectorPage != null && vectorPage.getContent() != null)
                    ? vectorPage.getContent()
                    :List.of();
        }, taskExecutor);

        // 병렬로 실행된 두 검색이 모두 완료되면 RRF로 통합
        CompletableFuture<List<BookSearchItemResponse>> fusedResultsFuture = keywordSearchFuture.thenCombineAsync(
                vectorSearchFuture,
                (keywordResults, vectorResults) -> {
                    return rrfService.fuse(keywordResults, vectorResults);
                },
                taskExecutor
        );

        // 최종 결과 가져오기
        List<BookSearchItemResponse> fusedResults = fusedResultsFuture.join();
        log.info("하이브리드 검색 완료: fused={}건", fusedResults.size());

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), fusedResults.size());

        if (start > fusedResults.size()) {
            return new PageImpl<>(List.of(), pageable, fusedResults.size());
        }

        List<BookSearchItemResponse> pageList = fusedResults.subList(start, end);

        return new PageImpl<>(pageList, pageable, fusedResults.size());
    }
}
