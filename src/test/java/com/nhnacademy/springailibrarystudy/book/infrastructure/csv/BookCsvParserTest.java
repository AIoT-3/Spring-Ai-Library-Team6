package com.nhnacademy.springailibrarystudy.book.infrastructure.csv;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookCsvParserTest {

    private static final String SAMPLE_CSV_PATH = "data/sample-books.csv";

    @Test
    void parseSampleCsv() throws URISyntaxException {
        // Given
        List<BookCsvRow> rows = new ArrayList<>();
        BookCsvParser.parse(sampleCsvPath(), rows::add);

        // When & Then
        assertAll(
                () -> assertEquals(395, rows.size()),
                () -> assertEquals("6352228", rows.getFirst().sourceSeqNo()),
                () -> assertEquals("9791156759270", rows.getFirst().isbn13()),
                () -> assertEquals("2021-12-03", rows.getFirst().publishedDate())
        );
    }

    private Path sampleCsvPath() throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(SAMPLE_CSV_PATH);
        assertNotNull(resource);
        return Path.of(resource.toURI());
    }
}
