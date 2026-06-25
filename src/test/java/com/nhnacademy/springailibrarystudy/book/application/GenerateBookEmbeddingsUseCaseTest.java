package com.nhnacademy.springailibrarystudy.book.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingGenerationOptions;
import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingGenerationResult;
import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingTarget;
import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookEmbeddingRepository;
import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

@ExtendWith(MockitoExtension.class)
class GenerateBookEmbeddingsUseCaseTest {

    @Mock
    EmbeddingModel embeddingModel;

    @Mock
    BookEmbeddingRepository bookEmbeddingRepository;

    @Spy
    BookEmbeddingSourceTextBuilder sourceTextBuilder = new BookEmbeddingSourceTextBuilder();

    @InjectMocks
    GenerateBookEmbeddingsUseCase generateBookEmbeddingsUseCase;

    @Test
    @DisplayName("임베딩 없는 도서를 배치로 생성")
    void generateMissingBookEmbeddings() {
        // given
        BookEmbeddingGenerationOptions options = new BookEmbeddingGenerationOptions("bge-m3", 10, 0);
        BookEmbeddingTarget target = new BookEmbeddingTarget(
                1L,
                "자바 입문",
                null,
                "홍길동",
                "한빛미디어",
                "자바 기초"
        );

        given(bookEmbeddingRepository.findCandidateTargetsAfter(0L, 10))
                .willReturn(List.of(target));
        given(bookEmbeddingRepository.findCandidateTargetsAfter(1L, 10))
                .willReturn(List.of());
        given(bookEmbeddingRepository.findExistingBookIds(eq("bge-m3"), anyMap()))
                .willReturn(Set.of());
        given(embeddingModel.embed(List.of("""
                제목: 자바 입문
                저자: 홍길동
                출판사: 한빛미디어
                소개: 자바 기초
                """.stripTrailing())))
                .willReturn(List.of(new float[]{0.1f, 0.2f, 0.3f}));
        given(bookEmbeddingRepository.batchInsert(eq("bge-m3"), anyList(), anyList(), anyList(), anyList()))
                .willReturn(1);

        // when
        BookEmbeddingGenerationResult result = generateBookEmbeddingsUseCase.generate(options);

        // then
        assertThat(result.embeddingModel()).isEqualTo("bge-m3");
        assertThat(result.targetCount()).isEqualTo(1);
        assertThat(result.insertedCount()).isEqualTo(1);

        ArgumentCaptor<List<BookEmbeddingTarget>> targetCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<List<String>> sourceTextCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<List<String>> sourceTextHashCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<List<float[]>> embeddingCaptor = ArgumentCaptor.captor();

        then(bookEmbeddingRepository).should()
                .batchInsert(
                        eq("bge-m3"),
                        targetCaptor.capture(),
                        sourceTextCaptor.capture(),
                        sourceTextHashCaptor.capture(),
                        embeddingCaptor.capture()
                );

        assertThat(targetCaptor.getValue()).containsExactly(target);
        assertThat(sourceTextCaptor.getValue()).containsExactly("""
                제목: 자바 입문
                저자: 홍길동
                출판사: 한빛미디어
                소개: 자바 기초
                """.stripTrailing());
        assertThat(sourceTextHashCaptor.getValue().getFirst())
                .hasSize(64)
                .matches("[0-9a-f]{64}");
        assertThat(embeddingCaptor.getValue().getFirst()).containsExactly(0.1f, 0.2f, 0.3f);
    }

    @Test
    @DisplayName("이미 생성된 소스 해시는 임베딩 호출에서 제외")
    void skipExistingSourceHash() {
        // given
        BookEmbeddingGenerationOptions options = new BookEmbeddingGenerationOptions("bge-m3", 10, 0);
        BookEmbeddingTarget existingTarget = new BookEmbeddingTarget(
                1L,
                "자바 입문",
                null,
                "홍길동",
                "한빛미디어",
                "자바 기초"
        );
        BookEmbeddingTarget missingTarget = new BookEmbeddingTarget(
                2L,
                "스프링 입문",
                "상권",
                "김길동",
                "길벗",
                "스프링 기초"
        );
        String missingSourceText = """
                제목: 스프링 입문
                권 정보: 상권
                저자: 김길동
                출판사: 길벗
                소개: 스프링 기초
                """.stripTrailing();

        given(bookEmbeddingRepository.findCandidateTargetsAfter(0L, 10))
                .willReturn(List.of(existingTarget, missingTarget));
        given(bookEmbeddingRepository.findCandidateTargetsAfter(2L, 10))
                .willReturn(List.of());
        given(bookEmbeddingRepository.findExistingBookIds(eq("bge-m3"), anyMap()))
                .willReturn(Set.of(existingTarget.bookId()));
        given(embeddingModel.embed(List.of(missingSourceText)))
                .willReturn(List.of(new float[]{0.4f, 0.5f, 0.6f}));
        given(bookEmbeddingRepository.batchInsert(eq("bge-m3"), anyList(), anyList(), anyList(), anyList()))
                .willReturn(1);

        // when
        BookEmbeddingGenerationResult result = generateBookEmbeddingsUseCase.generate(options);

        // then
        assertThat(result.targetCount()).isEqualTo(1);
        assertThat(result.insertedCount()).isEqualTo(1);

        ArgumentCaptor<List<BookEmbeddingTarget>> targetCaptor = ArgumentCaptor.captor();
        ArgumentCaptor<List<String>> sourceTextCaptor = ArgumentCaptor.captor();

        then(bookEmbeddingRepository).should()
                .batchInsert(
                        eq("bge-m3"),
                        targetCaptor.capture(),
                        sourceTextCaptor.capture(),
                        anyList(),
                        anyList()
                );

        assertThat(targetCaptor.getValue()).containsExactly(missingTarget);
        assertThat(sourceTextCaptor.getValue()).containsExactly(missingSourceText);
    }

    @Test
    @DisplayName("임베딩 결과 개수가 다르면 예외")
    void throwsWhenEmbeddingResultSizeDoesNotMatch() {
        // given
        BookEmbeddingGenerationOptions options = new BookEmbeddingGenerationOptions("bge-m3", 10, 0);
        BookEmbeddingTarget target = new BookEmbeddingTarget(
                1L,
                "자바 입문",
                null,
                "홍길동",
                "한빛미디어",
                "자바 기초"
        );

        given(bookEmbeddingRepository.findCandidateTargetsAfter(0L, 10))
                .willReturn(List.of(target));
        given(bookEmbeddingRepository.findExistingBookIds(eq("bge-m3"), anyMap()))
                .willReturn(Set.of());
        given(embeddingModel.embed(anyList()))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> generateBookEmbeddingsUseCase.generate(options))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BOOK_EMBEDDING_GENERATION_FAILED);
    }
}
