package com.nhnacademy.springailibrarystudy.book.domain;

import org.springframework.util.StringUtils;

public final class IsbnNormalizer {

    private static final String ISBN13_PREFIX_FOR_ISBN10 = "978";
    private static final int ISBN10_LENGTH = 10;
    private static final int ISBN13_LENGTH = 13;

    private IsbnNormalizer() {
    }

    public static String normalizeToIsbn13(String rawIsbn) {
        String compacted = compact(rawIsbn);

        String result = normalizeIsbn13(compacted);
        if (result != null) {
            return result;
        } else {
            return normalizeIsbn10ToIsbn13(compacted);
        }
    }

    public static String normalizeIsbn13(String rawIsbn) {
        String compacted = compact(rawIsbn);

        if (compacted.length() != ISBN13_LENGTH || !compacted.chars().allMatch(Character::isDigit)) {
            return null;
        }

        int expected = calculateIsbn13CheckDigit(compacted.substring(0, 12));
        int actual = Character.digit(compacted.charAt(12), 10);
        return expected == actual ? compacted : null;
    }

    public static String normalizeIsbn10ToIsbn13(String rawIsbn) {
        String compacted = compact(rawIsbn);

        if (compacted.length() != ISBN10_LENGTH) {
            return null;
        }

        String body = compacted.substring(0, 9);
        char checkDigit = compacted.charAt(9);

        if (!body.chars().allMatch(Character::isDigit)
                || (!Character.isDigit(checkDigit) && checkDigit != 'X')) {
            return null;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.digit(compacted.charAt(i), 10) * (10 - i);
        }
        sum += checkDigit == 'X' ? 10 : Character.digit(checkDigit, 10);

        if (sum % 11 != 0) {
            return null;
        }

        String isbn13Body = ISBN13_PREFIX_FOR_ISBN10 + body;
        return isbn13Body + calculateIsbn13CheckDigit(isbn13Body);
    }

    private static String compact(String rawIsbn) {
        if (!StringUtils.hasText(rawIsbn)) {
            return "";
        }

        return rawIsbn.toUpperCase()
                .replaceAll("[\\s-]", "");
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
