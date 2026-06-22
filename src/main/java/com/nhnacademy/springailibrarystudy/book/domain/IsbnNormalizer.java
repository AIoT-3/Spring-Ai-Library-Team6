package com.nhnacademy.springailibrarystudy.book.domain;

import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import java.util.Optional;
import org.springframework.util.StringUtils;

public final class IsbnNormalizer {

    private static final String ISBN13_PREFIX_FOR_ISBN10 = "978";

    private IsbnNormalizer() {
    }

    public static Optional<String> tryNormalizeToIsbn13(String rawIsbn) {
        try {
            return Optional.of(normalizeToIsbn13(rawIsbn));
        } catch (BusinessException e) {
            return Optional.empty();
        }
    }

    public static String normalizeToIsbn13(String rawIsbn) {
        if (!StringUtils.hasText(rawIsbn)) {
            throw new BusinessException(ErrorCode.INVALID_ISBN);
        }

        // 공백, 하이픈 제거 및 대문자 변환
        String compacted = rawIsbn.toUpperCase()
                .replaceAll("[\\s-]", "");

        // ISBN-13인 경우
        if (compacted.length() == 13) {
            return validateIsbn13(compacted);
        }

        // ISBN-10인 경우
        if (compacted.length() == 10) {
            return normalizeIsbn10(compacted);
        }

        throw new BusinessException(ErrorCode.INVALID_ISBN);
    }

    private static String validateIsbn13(String compacted) {
        // 모든 문자가 숫자인지 확인
        if (!compacted.chars().allMatch(Character::isDigit)) {
            throw new BusinessException(ErrorCode.INVALID_ISBN);
        }

        // ISBN-13 체크 디지트 검증
        int expected = calculateIsbn13CheckDigit(compacted.substring(0, 12));
        int actual = Character.digit(compacted.charAt(12), 10);
        if (expected != actual) {
            throw new BusinessException(ErrorCode.INVALID_ISBN);
        }
        return compacted;
    }

    private static String normalizeIsbn10(String compacted) {
        // 앞 9자리: 모두 숫자여야 함
        String body = compacted.substring(0, 9);
        if (!body.chars().allMatch(Character::isDigit)) {
            throw new BusinessException(ErrorCode.INVALID_ISBN);
        }

        // 마지막 문자: 숫자 또는 'X'이어야 함
        char checkDigit = compacted.charAt(9);
        if (!Character.isDigit(checkDigit) && checkDigit != 'X') {
            throw new BusinessException(ErrorCode.INVALID_ISBN);
        }

        // ISBN-10 체크 디지트 계산
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.digit(compacted.charAt(i), 10) * (10 - i);
        }
        sum += checkDigit == 'X' ? 10 : Character.digit(checkDigit, 10);

        // ISBN-10 체크 디지트 검증
        if (sum % 11 != 0) {
            throw new BusinessException(ErrorCode.INVALID_ISBN);
        }

        // ISBN-10을 ISBN-13으로 변환
        String isbn13Body = ISBN13_PREFIX_FOR_ISBN10 + body;
        return isbn13Body + calculateIsbn13CheckDigit(isbn13Body);
    }

    private static int calculateIsbn13CheckDigit(String firstTwelveDigits) {
        int sum = 0;
        for (int i = 0; i < firstTwelveDigits.length(); i++) {
            int digit = Character.digit(firstTwelveDigits.charAt(i), 10);
            sum += digit * (i % 2 == 0 ? 1 : 3);
        }
        return (10 - (sum % 10)) % 10;
    }
}
