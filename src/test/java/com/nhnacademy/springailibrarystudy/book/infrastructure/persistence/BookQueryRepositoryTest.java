package com.nhnacademy.springailibrarystudy.book.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.nhnacademy.springailibrarystudy.TestcontainersConfiguration;
import com.nhnacademy.springailibrarystudy.book.domain.Book;
import com.nhnacademy.springailibrarystudy.global.config.QuerydslConfig;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({
        TestcontainersConfiguration.class,
        QuerydslConfig.class,
        BookQueryRepository.class
})
@ActiveProfiles("test")
class BookQueryRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    BookQueryRepository bookQueryRepository;

    @Test
    @DisplayName("키워드는 여러 필드를 OR로 검색")
    void searchKeywordAcrossFields() {
        // given
        persistBook("9791156759270", "자바 입문", null, "김개발", "한빛미디어", "005.13");
        persistBook("9791168120877", "스프링 부트", null, "자바 연구회", "길벗", "005.11");
        persistBook("9791168120839", "데이터베이스", null, "박데이터", "자바출판", "005.74");
        persistBook("9791168120846", "철학 산책", null, "이철학", "민음사", "100");
        flushAndClear();

        // when
        Page<BookSearchItemResponse> result = bookQueryRepository.searchByKeyword(
                "자바",
                null,
                null,
                PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent())
                .extracting(BookSearchItemResponse::title)
                .containsExactly("자바 입문", "스프링 부트", "데이터베이스");
    }

    @Test
    @DisplayName("ISBN은 정확히 필터링")
    void filterByIsbn() {
        // given
        persistBook("9791156759270", "자바 입문", null, "김개발", "한빛미디어", "005.13");
        persistBook("9791168120877", "자바 고급", null, "이개발", "길벗", "005.13");
        flushAndClear();

        // when
        Page<BookSearchItemResponse> result = bookQueryRepository.searchByKeyword(
                "자바",
                "9791168120877",
                null,
                PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent())
                .extracting(BookSearchItemResponse::title)
                .containsExactly("자바 고급");
    }

    @Test
    @DisplayName("KDC는 prefix로 필터링")
    void filterByKdcPrefix() {
        // given
        persistBook("9791156759270", "자바 입문", null, "김개발", "한빛미디어", "005.13");
        persistBook("9791168120877", "자바 문학", null, "이작가", "문학사", "811.7");
        persistBook("9791168120839", "자바 역사", null, "박역사", "역사출판", "911");
        flushAndClear();

        // when
        Page<BookSearchItemResponse> result = bookQueryRepository.searchByKeyword(
                "자바",
                null,
                "81",
                PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent())
                .extracting(BookSearchItemResponse::title)
                .containsExactly("자바 문학");
    }

    private void persistBook(
            String isbn13,
            String title,
            String volumeTitle,
            String authorName,
            String publisherName,
            String kdcCode
    ) {
        entityManager.persist(Book.builder()
                .isbn13(isbn13)
                .volumeTitle(volumeTitle)
                .title(title)
                .authorName(authorName)
                .publisherName(publisherName)
                .publishedDate(LocalDate.of(2021, 12, 1))
                .price(BigDecimal.valueOf(15000))
                .imageUrl("https://example.com/book.jpg")
                .description("테스트 도서")
                .kdcCode(kdcCode)
                .build());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
