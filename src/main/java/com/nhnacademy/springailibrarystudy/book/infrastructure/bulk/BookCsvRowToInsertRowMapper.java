package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk;

import com.nhnacademy.springailibrarystudy.book.domain.IsbnNormalizer;
import com.nhnacademy.springailibrarystudy.book.infrastructure.csv.BookCsvRow;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class BookCsvRowToInsertRowMapper {

    private BookCsvRowToInsertRowMapper() {
    }

    public static BookInsertRow toInsertRow(BookCsvRow row) {
        return new BookInsertRow(
                normalizeIsbn13(row),
                blankToNull(row.volumeTitle()),
                blankToNull(row.title()),
                blankToNull(row.authorName()),
                blankToNull(row.publisherName()),
                parseDateOrNull(row.publishedDate()),
                parsePriceOrNull(row.price()),
                blankToNull(row.imageUrl()),
                blankToNull(row.description()),
                blankToNull(row.kdcCode())
        );
    }

    private static String normalizeIsbn13(BookCsvRow row) {
        String isbn13 = IsbnNormalizer.normalizeToIsbn13(row.isbn13());
        return isbn13 != null ? isbn13 : IsbnNormalizer.normalizeToIsbn13(row.isbnNo());
    }

    private static String blankToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.strip();
    }

    private static LocalDate parseDateOrNull(String value) {
        String text = blankToNull(value);
        if (text == null) {
            return null;
        }

        try {
            if (text.length() == 8) {
                return LocalDate.parse(text, DateTimeFormatter.BASIC_ISO_DATE);
            }
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static BigDecimal parsePriceOrNull(String value) {
        String text = blankToNull(value);
        if (text == null) {
            return null;
        }

        try {
            return new BigDecimal(text.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
