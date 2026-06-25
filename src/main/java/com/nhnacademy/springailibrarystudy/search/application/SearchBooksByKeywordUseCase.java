package com.nhnacademy.springailibrarystudy.search.application;

import com.nhnacademy.springailibrarystudy.book.domain.IsbnNormalizer;
import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookQueryRepository;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SearchBooksByKeywordUseCase {

    private final BookQueryRepository bookQueryRepository;

    public Page<BookSearchItemResponse> search(BookSearchRequest request, Pageable pageable) {
        String query = normalize(request.query());
        String isbn13 = IsbnNormalizer.normalizeToIsbn13(request.isbn());
        String kdcCode = normalize(request.kdcCode());

        return bookQueryRepository.searchByKeyword(query, isbn13, kdcCode, pageable);
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
