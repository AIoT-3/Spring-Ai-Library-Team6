package com.nhnacademy.springailibrarystudy.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookQueryRepository;
import com.nhnacademy.springailibrarystudy.search.domain.SearchType;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class SearchBooksByKeywordUseCaseTest {

    @Mock
    BookQueryRepository bookQueryRepository;

    @InjectMocks
    SearchBooksByKeywordUseCase searchBooksByKeywordUseCase;

    @Test
    @DisplayName("검색 조건 정규화")
    void normalizesSearchConditions() {
        // given
        BookSearchRequest request = new BookSearchRequest(
                "  자바  ",
                "0-306-40615-2",
                "2  ",
                SearchType.KEYWORD
        );
        Pageable pageable = PageRequest.of(0, 20);
        Page<BookSearchItemResponse> expected = new PageImpl<>(List.of(), pageable, 0);

        given(bookQueryRepository.searchByKeyword("자바", "9780306406157", "2", pageable))
                .willReturn(expected);

        // when
        Page<BookSearchItemResponse> actual = searchBooksByKeywordUseCase.search(request, pageable);

        // then
        assertThat(actual).isSameAs(expected);
        then(bookQueryRepository).should().searchByKeyword("자바", "9780306406157", "2", pageable);
    }
}
