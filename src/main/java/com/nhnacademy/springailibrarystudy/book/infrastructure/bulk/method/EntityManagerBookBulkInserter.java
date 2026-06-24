package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.method;

import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookBulkInsertOptions;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookBulkInsertResult;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookBulkInserter;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookCsvRowToInsertRowMapper;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookInsertRow;
import com.nhnacademy.springailibrarystudy.book.infrastructure.csv.BookCsvParser;
import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

@Component
@RequiredArgsConstructor
public class EntityManagerBookBulkInserter implements BookBulkInserter {

    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    @Override
    public String name() {
        return "EntityManager";
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
                persistChunk(chunk);
            }
            return true;
        });
        persistChunk(chunk); // 남은 chunk 처리
        stopWatch.stop();

        // 결과 반환
        return BookBulkInsertResult.of(
                name(),
                options.batchSize(),
                rowCount.get(),
                stopWatch.getTotalTimeMillis()
        );
    }

    private void persistChunk(List<BookInsertRow> chunk) {
        if (chunk.isEmpty()) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            for (BookInsertRow row : chunk) {
                entityManager.persist(BookInsertRow.toBook(row));
            }
            entityManager.flush();
            entityManager.clear();
        });
        chunk.clear();
    }
}
