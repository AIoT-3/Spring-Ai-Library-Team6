package com.nhnacademy.springailibrarystudy.book.infrastructure.csv;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Tag("analysis")
class BookCsvSearchTargetProfileTest {

    private static final Path CSV_PATH = Path.of("src/main/resources/data/BOOK_DB_202112.csv");

    @Test
    void profileBookCsvSearchTarget() {
        Table table = Table.read().usingOptions(CsvReadOptions.builder(CSV_PATH.toString())
                .header(true)
                .columnTypes(stringColumnTypes())
                .build());

        log.info("\n{}\n", searchTargetLengthProfile(table).printAll());
    }

    // 모든 컬럼을 STRING 타입으로 읽도록 설정
    private ColumnType[] stringColumnTypes() {
        return IntStream.range(0, BookCsvHeader.values().length)
                .mapToObj(index -> ColumnType.STRING)
                .toArray(ColumnType[]::new);
    }

    // 검색 후보 컬럼의 길이 분포 분석
    private Table searchTargetLengthProfile(Table table) {
        List<BookCsvHeader> searchTargets = List.of(
                BookCsvHeader.TITLE_NM,
                BookCsvHeader.VLM_NM,
                BookCsvHeader.AUTHR_NM,
                BookCsvHeader.PUBLISHER_NM,
                BookCsvHeader.BOOK_INTRCN_CN,
                BookCsvHeader.KDC_NM,
                BookCsvHeader.ISBN_THIRTEEN_NO
        );

        StringColumn columnName = StringColumn.create("column");
        IntColumn filled = IntColumn.create("filled");
        DoubleColumn averageLength = DoubleColumn.create("avgLength");
        IntColumn p50 = IntColumn.create("p50");
        IntColumn p90 = IntColumn.create("p90");
        IntColumn p95 = IntColumn.create("p95");
        IntColumn p99 = IntColumn.create("p99");
        IntColumn maxLength = IntColumn.create("maxLength");
        IntColumn unique = IntColumn.create("unique");
        DoubleColumn uniqueRate = DoubleColumn.create("uniqueRate");

        for (BookCsvHeader header : searchTargets) {
            Column<?> column = table.column(header.name());
            List<Integer> lengths = filledLengths(column);
            int filledCount = lengths.size();

            columnName.append(header.name());
            filled.append(filledCount);
            averageLength.append(averageLength(lengths));
            p50.append(percentile(lengths, 50));
            p90.append(percentile(lengths, 90));
            p95.append(percentile(lengths, 95));
            p99.append(percentile(lengths, 99));
            maxLength.append(lengths.isEmpty() ? 0 : lengths.getLast());
            unique.append(column.removeMissing().countUnique());
            uniqueRate.append(filledCount == 0 ? 0.0 : column.removeMissing().countUnique() * 100.0 / filledCount);
        }

        return Table.create("Book CSV Search Target Length 분석",
                columnName, filled, averageLength, p50, p90, p95, p99, maxLength, unique, uniqueRate);
    }

    private List<Integer> filledLengths(Column<?> column) {
        return IntStream.range(0, column.size())
                .mapToObj(column::getString)
                .filter(value -> !StringUtils.isBlank(value))
                .map(String::length)
                .sorted()
                .toList();
    }

    private double averageLength(List<Integer> lengths) {
        return lengths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    private int percentile(List<Integer> sortedLengths, int percentile) {
        if (sortedLengths.isEmpty()) {
            return 0;
        }

        int index = (int) Math.ceil(sortedLengths.size() * percentile / 100.0) - 1;
        return sortedLengths.get(Math.max(index, 0));
    }
}
