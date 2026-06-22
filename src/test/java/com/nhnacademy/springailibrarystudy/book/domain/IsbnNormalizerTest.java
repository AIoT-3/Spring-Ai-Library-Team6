package com.nhnacademy.springailibrarystudy.book.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
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
    @DisplayName("잘못된 ISBN check digit은 예외")
    void rejectInvalidCheckDigit() {
        assertThatThrownBy(() -> IsbnNormalizer.normalizeToIsbn13("0-306-40615-3"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_ISBN));
    }

    @Test
    @DisplayName("ISBN 접두어는 허용하지 않고 예외")
    void rejectIsbnPrefix() {
        assertThatThrownBy(() -> IsbnNormalizer.normalizeToIsbn13("ISBN-10: 0-306-40615-2"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_ISBN));
    }

    @Test
    @DisplayName("ISBN이 아니면 Optional.empty 반환")
    void tryNormalizeReturnsEmpty() {
        assertThat(IsbnNormalizer.tryNormalizeToIsbn13("spring cache")).isEmpty();
    }
}
