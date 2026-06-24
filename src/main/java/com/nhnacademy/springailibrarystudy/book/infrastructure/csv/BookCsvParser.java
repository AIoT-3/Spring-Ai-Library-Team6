package com.nhnacademy.springailibrarystudy.book.infrastructure.csv;

import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public final class BookCsvParser {
    private BookCsvParser() {
    }

    public static void parse(Path path, Consumer<BookCsvRow> consumer) {
        parseWhile(path, row -> {
            consumer.accept(row);
            return true;
        });
    }

    public static void parseWhile(Path path, BookCsvRowHandler rowHandler) {
        try (
                Reader reader = Files.newBufferedReader(path);
                CSVParser parser = CSVFormat.DEFAULT.parse(reader)
        ) {
            Iterator<CSVRecord> records = parser.iterator();
            if (!records.hasNext()) {
                throw new BusinessException(ErrorCode.INVALID_BOOK_CSV);
            }

            List<String> headers = records.next().toList();
            if (!headers.isEmpty() && headers.getFirst().startsWith("\uFEFF")) {
                headers = new ArrayList<>(headers);
                headers.set(0, headers.get(0).substring(1));
            }
            BookCsvRow.validateHeaders(headers);

            // 데이터 행 처리
            while (records.hasNext()) {
                CSVRecord csvRecord = records.next();
                if (!rowHandler.handle(BookCsvRow.from(csvRecord))) {
                    break;
                }
            }

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BOOK_CSV_READ_FAILED, e);
        }
    }

}
