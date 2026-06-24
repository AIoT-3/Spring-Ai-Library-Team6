package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

@Slf4j
@Tag("analysis")
@SpringBootTest
@ActiveProfiles("bulk-test")
@EnabledIfSystemProperty(named = "bulk.insert.performance", matches = "true")
class BookBulkInsertPerformanceTest {

    private static final Path DEFAULT_CSV_PATH = Path.of("src/main/resources/data/BOOK_DB_202112.csv");
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int DEFAULT_ROW_LIMIT = 157_118;

    @Autowired
    BookBulkInsertRunner runner;

    @Test
    void compareBulkInsertMethods() {
        BookBulkInsertOptions options = new BookBulkInsertOptions(
                csvPath(),
                batchSize(),
                rowLimit()
        );

        List<BookBulkInsertResult> results = runner.runAll(options);

        log.info("\n{}\n", toTable(options, results).printAll());

        int expectedRowCount = results.isEmpty() ? 0 : results.getFirst().rowCount();
        assertAll(
                () -> assertFalse(results.isEmpty()),
                () -> assertTrue(expectedRowCount > 0),
                () -> results.forEach(result -> assertEquals(expectedRowCount, result.rowCount())),
                () -> results.forEach(result -> assertTrue(result.elapsedMillis() >= 0)),
                () -> results.forEach(result -> assertTrue(result.rowsPerSecond() >= 0))
        );
    }

    private Path csvPath() {
        return Path.of(System.getProperty("bulk.insert.csv", DEFAULT_CSV_PATH.toString()));
    }

    private int batchSize() {
        return Integer.getInteger("bulk.insert.batch-size", DEFAULT_BATCH_SIZE);
    }

    private int rowLimit() {
        return Integer.getInteger("bulk.insert.row-limit", DEFAULT_ROW_LIMIT);
    }

    private Table toTable(BookBulkInsertOptions options, List<BookBulkInsertResult> results) {
        StringColumn method = StringColumn.create("method");
        IntColumn batchSize = IntColumn.create("batchSize");
        IntColumn rowLimit = IntColumn.create("rowLimit");
        IntColumn rowCount = IntColumn.create("rowCount");
        LongColumn elapsedMillis = LongColumn.create("elapsedMillis");
        DoubleColumn rowsPerSecond = DoubleColumn.create("rowsPerSecond");

        for (BookBulkInsertResult result : results) {
            method.append(result.methodName());
            batchSize.append(result.batchSize());
            rowLimit.append(options.rowLimit());
            rowCount.append(result.rowCount());
            elapsedMillis.append(result.elapsedMillis());
            rowsPerSecond.append(result.rowsPerSecond());
        }

        return Table.create(
                "Book Bulk Insert Performance",
                method,
                batchSize,
                rowLimit,
                rowCount,
                elapsedMillis,
                rowsPerSecond
        );
    }
}
