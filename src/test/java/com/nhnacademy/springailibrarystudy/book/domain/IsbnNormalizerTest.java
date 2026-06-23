package com.nhnacademy.springailibrarystudy.book.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IsbnNormalizerTest {

    @Test
    @DisplayName("하이픈 있는 ISBN-10을 ISBN-13으로 변환")
    void normalizeIsbn10ToIsbn13() {
        String isbn13 = IsbnNormalizer.normalizeToIsbn13("0-306-40615-2");

        assertThat(isbn13).isEqualTo("9780306406157");
    }

    @Test
    @DisplayName("하이픈 있는 ISBN-13 정규화")
    void normalizeIsbn13() {
        String isbn13 = IsbnNormalizer.normalizeToIsbn13("978-0-306-40615-7");

        assertThat(isbn13).isEqualTo("9780306406157");
    }

    @Test
    @DisplayName("공백은 제거하고 정규화")
    void normalizeWithWhitespace() {
        String isbn13 = IsbnNormalizer.normalizeToIsbn13("978 0 306 40615 7");

        assertThat(isbn13).isEqualTo("9780306406157");
    }

    @Test
    @DisplayName("잘못된 ISBN check digit은 null 반환")
    void returnNullForInvalidCheckDigit() {
        assertThat(IsbnNormalizer.normalizeToIsbn13("0-306-40615-3")).isNull();
    }

    @Test
    @DisplayName("ISBN 접두어는 허용하지 않고 null 반환")
    void returnNullForIsbnPrefix() {
        assertThat(IsbnNormalizer.normalizeToIsbn13("ISBN-10: 0-306-40615-2")).isNull();
    }

    @Test
    @DisplayName("ISBN이 아니면 null 반환")
    void returnNullForNonIsbn() {
        assertThat(IsbnNormalizer.normalizeToIsbn13("spring cache")).isNull();
    }

    @Test
    @DisplayName("ISBN-13 전용 정규화")
    void normalizeOnlyIsbn13() {
        assertThat(IsbnNormalizer.normalizeIsbn13("978-0-306-40615-7")).isEqualTo("9780306406157");
        assertThat(IsbnNormalizer.normalizeIsbn13("0-306-40615-2")).isNull();
    }

    @Test
    @DisplayName("ISBN-10 전용 ISBN-13 변환")
    void normalizeOnlyIsbn10ToIsbn13() {
        assertThat(IsbnNormalizer.normalizeIsbn10ToIsbn13("0-306-40615-2")).isEqualTo("9780306406157");
        assertThat(IsbnNormalizer.normalizeIsbn10ToIsbn13("978-0-306-40615-7")).isNull();
    }
}
