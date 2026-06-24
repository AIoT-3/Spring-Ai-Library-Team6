package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.method;

import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookBulkInsertOptions;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookBulkInsertResult;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookBulkInserter;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookCsvRowToInsertRowMapper;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookInsertRow;
import com.nhnacademy.springailibrarystudy.book.infrastructure.csv.BookCsvParser;
import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

@Component
@RequiredArgsConstructor
public class CopyManagerBookBulkInserter implements BookBulkInserter {

    private final DataSource dataSource;
    private final TransactionTemplate transactionTemplate;

    private static final String COPY_SQL = """
            copy books (
                isbn13, volume_title, title, author_name, publisher_name,
                published_date, price, image_url, description, kdc_code
            )
            from stdin with (format text, delimiter E'\\t', null '\\N')
            """;

    @Override
    public String name() {
        return "CopyManager";
    }

    @Override
    public BookBulkInsertResult insert(BookBulkInsertOptions options) {
        if (options.batchSize() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_BULK_INSERT_OPTIONS);
        }

        AtomicInteger rowCount = new AtomicInteger(); // 람다 쓰려면 AtomicInteger 사용
        StopWatch stopWatch = new StopWatch();

        // 파싱 및 DB 저장
        stopWatch.start();
        transactionTemplate.executeWithoutResult(status -> {
            // transactionTemplate 안의 connection은 Spring이 관리하므로, DataSourceUtils를 사용하여 가져옴
            Connection connection = DataSourceUtils.getConnection(dataSource);
            CopyIn copyIn = null;

            try {
                CopyManager copyManager = new CopyManager(connection.unwrap(BaseConnection.class));
                copyIn = copyManager.copyIn(COPY_SQL);
                CopyIn currentCopy = copyIn; // 람다 안에서 사용하기 위해 final 변수로 선언

                BookCsvParser.parseWhile(options.csvPath(), csvRow -> {
                    if (options.rowLimit() > 0 && rowCount.get() >= options.rowLimit()) {
                        return false;
                    }

                    BookInsertRow row = BookCsvRowToInsertRowMapper.toInsertRow(csvRow);
                    writeRow(currentCopy, row);
                    rowCount.incrementAndGet();
                    return true;
                });

                currentCopy.endCopy();
            } catch (SQLException e) {
                cancelCopy(copyIn);
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, e);
            } catch (RuntimeException e) {
                cancelCopy(copyIn);
                throw e;
            } finally {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        });
        stopWatch.stop();

        return BookBulkInsertResult.of(
                name(),
                options.batchSize(),
                rowCount.get(),
                stopWatch.getTotalTimeMillis()
        );
    }

    private void writeRow(CopyIn copyIn, BookInsertRow row) {
        try {
            byte[] bytes = (toCopyLine(row) + "\n").getBytes(StandardCharsets.UTF_8);
            copyIn.writeToCopy(bytes, 0, bytes.length);
        } catch (SQLException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, e);
        }
    }

    // COPY는 tab으로 구분된 텍스트를 사용하므로, 각 컬럼을 tab으로 join하고 null은 \N으로 변환
    private String toCopyLine(BookInsertRow row) {
        return String.join("\t",
                copyValue(row.isbn13()),
                copyValue(row.volumeTitle()),
                copyValue(row.title()),
                copyValue(row.authorName()),
                copyValue(row.publisherName()),
                copyValue(row.publishedDate()),
                row.price() == null ? "\\N" : row.price().toPlainString(),
                copyValue(row.imageUrl()),
                copyValue(row.description()),
                copyValue(row.kdcCode())
        );
    }

    // 값 안의 특수 문자들을 COPY 명령어에서 인식할 수 있도록 escape 형태로 변환
    private String copyValue(Object value) {
        if (value == null) {
            return "\\N"; // PostgreSQL COPY 명령어에서 NULL을 나타내는 값
        }

        return value.toString()
                .replace("\\", "\\\\") // 백슬래시 보존
                .replace("\t", "\\t")  // 탭 문자를 \t로 변환
                .replace("\n", "\\n")  // 줄바꿈 문자를 \n로 변환
                .replace("\r", "\\r"); // 캐리지 리턴 문자를 \r로 변환
    }

    private void cancelCopy(CopyIn copyIn) {
        try {
            if (copyIn != null && copyIn.isActive()) {
                copyIn.cancelCopy();
            }
        } catch (SQLException ignored) {
            // 원래 실패 원인을 가리지 않기 위해 COPY 취소 실패는 무시
        }
    }
}
