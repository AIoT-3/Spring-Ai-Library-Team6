package com.nhnacademy.springailibrarystudy.book.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.nhnacademy.springailibrarystudy.book.application.dto.BookEmbeddingTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookEmbeddingSourceTextBuilderTest {

    private final BookEmbeddingSourceTextBuilder builder = new BookEmbeddingSourceTextBuilder();

    @Test
    @DisplayName("임베딩 소스 텍스트 생성")
    void buildSourceText() {
        // given
        BookEmbeddingTarget target = new BookEmbeddingTarget(
                1L,
                "  자바   입문  ",
                " 상권 ",
                " 홍길동  지음 ; 김검수 감수 ",
                " 한빛미디어 ",
                " <p>처음&nbsp;배우는 <b>C++</b>와 TCP/IP</p> "
        );

        // when
        String sourceText = builder.build(target);

        // then
        assertThat(sourceText).isEqualTo("""
                제목: 자바 입문
                권 정보: 상권
                저자: 홍길동 지음 ; 김검수 감수
                출판사: 한빛미디어
                소개: 처음 배우는 C++와 TCP/IP
                """.stripTrailing());
    }

    @Test
    @DisplayName("소스 텍스트 해시 생성")
    void hashSourceText() {
        assertThat(builder.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
