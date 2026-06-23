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
import java.util.function.Consumer;

@Slf4j
public final class BookCsvParser {
    private BookCsvParser() {
    }

    public static void parse(Path path, Consumer<BookCsvRow> consumer) {
        try (
                Reader reader = Files.newBufferedReader(path);
                CSVParser parser = CSVFormat.DEFAULT.parse(reader)
        ) {
            boolean headerChecked = false;

            for (CSVRecord csvRecord : parser) {
                if (!headerChecked) {
                    BookCsvRow.validateHeaders(csvRecord.toList());
                    headerChecked = true;
                    continue;
                }

                consumer.accept(BookCsvRow.from(csvRecord));
            }

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BOOK_CSV_READ_FAILED, e);
        }
    }

}
