package com.nhnacademy.springailibrarystudy.book.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.nhnacademy.springailibrarystudy.TestcontainersConfiguration;
import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingTarget;
import com.nhnacademy.springailibrarystudy.book.domain.Book;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({
        TestcontainersConfiguration.class,
        BookEmbeddingRepository.class
})
@ActiveProfiles("test")
class BookEmbeddingRepositoryTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    BookEmbeddingRepository bookEmbeddingRepository;

    @Test
    @DisplayName("같은 모델과 소스 해시는 대상에서 제외")
    void insertEmbeddingAndExcludeExistingSourceHash() {
        // given
        Book indexedBook = persistBook("자바 입문");
        Book missingBook = persistBook("스프링 입문");
        flushAndClear();
        String indexedSourceHash = "a".repeat(64);
        String changedSourceHash = "b".repeat(64);

        BookEmbeddingTarget indexedTarget = new BookEmbeddingTarget(
                indexedBook.getId(),
                "자바 입문",
                null,
                "홍길동",
                "테스트출판",
                "테스트 설명"
        );

        // when
        int insertedCount = bookEmbeddingRepository.batchInsert(
                "bge-m3",
                List.of(indexedTarget),
                List.of("제목: 자바 입문"),
                List.of(indexedSourceHash),
                List.of(embedding())
        );
        int duplicatedCount = bookEmbeddingRepository.batchInsert(
                "bge-m3",
                List.of(indexedTarget),
                List.of("제목: 자바 입문"),
                List.of(indexedSourceHash),
                List.of(embedding())
        );
        int changedSourceCount = bookEmbeddingRepository.batchInsert(
                "bge-m3",
                List.of(indexedTarget),
                List.of("제목: 자바 입문 - 다른 조합"),
                List.of(changedSourceHash),
                List.of(embedding())
        );
        Set<Long> existingBookIds = bookEmbeddingRepository.findExistingBookIds(
                "bge-m3",
                Map.of(
                        indexedBook.getId(), indexedSourceHash,
                        missingBook.getId(), indexedSourceHash
                )
        );
        Set<Long> changedSourceBookIds = bookEmbeddingRepository.findExistingBookIds(
                "bge-m3",
                Map.of(indexedBook.getId(), "c".repeat(64))
        );
        List<BookEmbeddingTarget> targets = bookEmbeddingRepository.findCandidateTargetsAfter(0L, 10);

        // then
        assertThat(insertedCount).isEqualTo(1);
        assertThat(duplicatedCount).isZero();
        assertThat(changedSourceCount).isEqualTo(1);
        assertThat(existingBookIds).containsExactly(indexedBook.getId());
        assertThat(changedSourceBookIds).isEmpty();
        assertThat(targets)
                .extracting(BookEmbeddingTarget::bookId)
                .containsExactly(indexedBook.getId(), missingBook.getId());
    }

    private Book persistBook(String title) {
        Book book = Book.builder()
                .title(title)
                .authorName("홍길동")
                .publisherName("테스트출판")
                .publishedDate(LocalDate.of(2021, 12, 1))
                .price(BigDecimal.valueOf(15000))
                .description("테스트 설명")
                .kdcCode("005.13")
                .build();
        entityManager.persist(book);
        return book;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private float[] embedding() {
        float[] embedding = new float[1024];
        embedding[0] = 0.1f;
        embedding[1] = 0.2f;
        embedding[2] = 0.3f;
        return embedding;
    }
}
