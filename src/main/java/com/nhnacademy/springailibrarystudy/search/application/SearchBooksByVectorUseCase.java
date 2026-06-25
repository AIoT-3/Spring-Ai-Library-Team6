package com.nhnacademy.springailibrarystudy.search.application;

import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchBooksByVectorUseCase {

    public Page<BookSearchItemResponse> search(BookSearchRequest request, Pageable pageable) {
        throw new UnsupportedOperationException("벡터 검색 구현이 필요합니다.");
    }
}
