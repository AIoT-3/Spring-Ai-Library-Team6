package com.nhnacademy.springailibrarystudy.ai.agent.tools;

import com.nhnacademy.springailibrarystudy.ai.agent.dto.BookSearchToolResult;
import com.nhnacademy.springailibrarystudy.search.application.SearchBooksHybridUseCase;
import com.nhnacademy.springailibrarystudy.search.domain.SearchType;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSearchTool {

    private static final int SEARCH_RESULT_LIMIT = 5;

    private final SearchBooksHybridUseCase searchBooksHybridUseCase;

    @Tool(description = "도서관 시스템에서 제목이나 저자명으로 도서를 검색합니다. 사용자가 도서를 추천해달라고 하거나, 찾아달라는 등, 검색 의도를 가질 때 이 도구를 사용합니다.")
    public List<BookSearchToolResult> searchBooks(
            @ToolParam(description = "검색어 (도서 제목, 저자명 등)") String query
    ) {
        log.info("[AI Tool] 도서 검색 시작: query={}", query);

        BookSearchRequest request = new BookSearchRequest(query, null, null, SearchType.HYBRID, null);
        List<BookSearchItemResponse> books = searchBooksHybridUseCase
                .search(request, PageRequest.of(0, SEARCH_RESULT_LIMIT))
                .getContent();

        log.info("[AI Tool] 도서 검색 완료: {}건", books.size());
        return books.stream()
                .map(BookSearchToolResult::from)
                .toList();
    }

}
