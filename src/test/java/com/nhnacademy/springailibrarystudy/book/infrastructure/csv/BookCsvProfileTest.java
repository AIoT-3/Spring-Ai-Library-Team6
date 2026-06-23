package com.nhnacademy.springailibrarystudy.book.infrastructure.csv;

import com.nhnacademy.springailibrarystudy.book.domain.IsbnNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
class BookCsvProfileTest {

    private static final Path CSV_PATH = Path.of("src/main/resources/data/BOOK_DB_202112.csv");

    @Tag("analysis")
    @Test
    void profileBookCsv() {
        Assumptions.assumeTrue(Boolean.getBoolean("csv.profile"));

        Table table = Table.read().usingOptions(CsvReadOptions.builder(CSV_PATH.toString())
                .header(true)
                .columnTypes(stringColumnTypes())
                .build());

        log.info("\n{}\n", summary(table).printAll());
        log.info("\n{}\n", columnProfile(table).printAll());
        log.info("\n{}\n", isbnProfile(table).printAll());
    }

    // 모든 컬럼을 STRING 타입으로 읽도록 설정
    private ColumnType[] stringColumnTypes() {
        return IntStream.range(0, BookCsvHeader.values().length)
                .mapToObj(index -> ColumnType.STRING)
                .toArray(ColumnType[]::new);
    }

    // 기본 분석 정보 생성: 파일 이름, 행 수, 열 수, 헤더 일치 여부
    private Table summary(Table table) {
        StringColumn metric = StringColumn.create("metric", List.of(
                "file",
                "rows",
                "columns",
                "headerMatches"
        ));
        StringColumn value = StringColumn.create("value", List.of(
                CSV_PATH.getFileName().toString(),
                String.valueOf(table.rowCount()),
                String.valueOf(table.columnCount()),
                String.valueOf(BookCsvHeader.csvNames().equals(table.columnNames()))
        ));

        return Table.create("Book CSV 요약", metric, value);
    }

    // 각 컬럼에 대한 분석 정보 생성: 이름, 타입, 채워진 값 수, 누락된 값 수, 채움 비율, 고유 값 수
    private Table columnProfile(Table table) {
        StringColumn columnName = StringColumn.create("column");
        IntColumn filled = IntColumn.create("filled");
        IntColumn missing = IntColumn.create("missing");
        DoubleColumn fillRateColumn = DoubleColumn.create("fillRate");
        IntColumn unique = IntColumn.create("unique");

        for (Column<?> column : table.columns()) {
            int missingCount = column.countMissing();
            int filledCount = table.rowCount() - missingCount;
            double fillRate = table.rowCount() == 0 ? 0.0 : filledCount * 100.0 / table.rowCount();

            columnName.append(column.name());
            filled.append(filledCount);
            missing.append(missingCount);
            fillRateColumn.append(fillRate);
            unique.append(column.removeMissing().countUnique()); // 고유 값 수 계산 (누락된 값 제외)
        }

        return Table.create("Book CSV Column 분석",
                columnName, filled, missing, fillRateColumn, unique);
    }

    // ISBN-13, ISBN-10 유효성 조합별 건수 집계
    private Table isbnProfile(Table table) {
        int bothValidCount = 0;
        int onlyIsbn13ValidCount = 0;
        int onlyIsbn10ValidCount = 0;
        int bothInvalidCount = 0;

        Column<?> isbn13Column = table.column(BookCsvHeader.ISBN_THIRTEEN_NO.name());
        Column<?> isbn10Column = table.column(BookCsvHeader.ISBN_NO.name());

        for (int row = 0; row < table.rowCount(); row++) {
            String isbn13 = isbn13Column.getString(row);
            String isbn10 = isbn10Column.getString(row);
            boolean isbn13Valid = IsbnNormalizer.normalizeIsbn13(isbn13) != null;
            boolean isbn10Valid = IsbnNormalizer.normalizeIsbn10ToIsbn13(isbn10) != null;

            if (isbn13Valid && isbn10Valid) {
                bothValidCount++;
            } else if (isbn13Valid) {
                onlyIsbn13ValidCount++;
            } else if (isbn10Valid) {
                onlyIsbn10ValidCount++;
            } else {
                bothInvalidCount++;
            }
        }

        StringColumn isbn13Status = StringColumn.create("isbn13");
        StringColumn isbn10Status = StringColumn.create("isbn10");
        IntColumn count = IntColumn.create("count");
        DoubleColumn rate = DoubleColumn.create("rate");

        appendIsbnProfileRow(isbn13Status, isbn10Status, count, rate,
                "valid", "valid", bothValidCount, table.rowCount());
        appendIsbnProfileRow(isbn13Status, isbn10Status, count, rate,
                "valid", "invalid", onlyIsbn13ValidCount, table.rowCount());
        appendIsbnProfileRow(isbn13Status, isbn10Status, count, rate,
                "invalid", "valid", onlyIsbn10ValidCount, table.rowCount());
        appendIsbnProfileRow(isbn13Status, isbn10Status, count, rate,
                "invalid", "invalid", bothInvalidCount, table.rowCount());

        return Table.create("Book CSV ISBN 분석", isbn13Status, isbn10Status, count, rate);
    }

    private void appendIsbnProfileRow(
            StringColumn isbn13Status,
            StringColumn isbn10Status,
            IntColumn count,
            DoubleColumn rate,
            String isbn13,
            String isbn10,
            int categoryCount,
            int totalCount
    ) {
        isbn13Status.append(isbn13);
        isbn10Status.append(isbn10);
        count.append(categoryCount);
        rate.append(totalCount == 0 ? 0.0 : categoryCount * 100.0 / totalCount);
    }
}
