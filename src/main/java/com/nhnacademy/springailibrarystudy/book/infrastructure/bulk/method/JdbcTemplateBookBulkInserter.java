package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.method;

import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.*;
import com.nhnacademy.springailibrarystudy.book.infrastructure.csv.BookCsvParser;
import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class JdbcTemplateBookBulkInserter implements BookBulkInserter {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate; // 명시적 커밋을 위해 추가

    private static final String SQL = """
            insert into books (
                id, isbn13, volume_title, title, author_name, publisher_name, published_date,
                price, image_url, description, kdc_code, created_at, updated_at
            )
            values (
                nextval('book_sequence'),
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                current_timestamp,
                current_timestamp
            )
            """;

    @Override
    public String name() {
        return "JdbcTemplate";
    }

    @Override
    public BookBulkInsertResult insert(BookBulkInsertOptions options) {
        if (options.batchSize() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_BULK_INSERT_OPTIONS);
        }

        // Batch 단위의 List 생성 및 측정 준비
        List<BookInsertRow> chunk = new ArrayList<>(options.batchSize());
        AtomicInteger rowCount = new AtomicInteger(); // 람다 쓰려면 AtomicInteger 사용
        StopWatch stopWatch = new StopWatch();

        // 파싱 및 DB 저장
        stopWatch.start();
        BookCsvParser.parseWhile(options.csvPath(), csvRow -> {
            // rowCount가 rowLimit를 초과하면 더 이상 처리 안함
            if (options.rowLimit() > 0 && rowCount.get() >= options.rowLimit()) {
                return false;
            }

            // CSV 행을 BookInsertRow로 변환하여 chunk에 추가
            chunk.add(BookCsvRowToInsertRowMapper.toInsertRow(csvRow));
            rowCount.incrementAndGet();

            // chunk의 크기가 batchSize에 도달하면 DB에 저장
            if (chunk.size() == options.batchSize()) {
                batchInsertChunk(chunk);
            }
            return true;
        });
        batchInsertChunk(chunk); // 남은 chunk 처리
        stopWatch.stop();

        // 결과 반환
        return BookBulkInsertResult.of(
                name(),
                options.batchSize(),
                rowCount.get(),
                stopWatch.getTotalTimeMillis()
        );
    }

    private void batchInsertChunk(List<BookInsertRow> chunk) {
        if (chunk.isEmpty()) {
            return;
        }

        transactionTemplate.executeWithoutResult(status ->
            jdbcTemplate.batchUpdate(SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    BookInsertRow row = chunk.get(i);

                    ps.setString(1, row.isbn13());
                    ps.setString(2, row.volumeTitle());
                    ps.setString(3, row.title());
                    ps.setString(4, row.authorName());
                    ps.setString(5, row.publisherName());
                    ps.setObject(6, row.publishedDate());
                    ps.setBigDecimal(7, row.price());
                    ps.setString(8, row.imageUrl());
                    ps.setString(9, row.description());
                    ps.setString(10, row.kdcCode());
                }

                @Override
                public int getBatchSize() {
                    return chunk.size();
                }
            })
        );
        chunk.clear();
    }
}
