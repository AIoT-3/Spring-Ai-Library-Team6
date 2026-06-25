package com.nhnacademy.springailibrarystudy.book.infrastructure.runner;

import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookBulkInsertOptions;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookBulkInsertResult;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookBulkInsertRunner;
import com.nhnacademy.springailibrarystudy.book.infrastructure.bulk.BookBulkInserter;
import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(0)
@ConditionalOnProperty(
        prefix = "book.bulk-insert.runner",
        name = "enabled",
        havingValue = "true"
)
public class BookBulkInsertApplicationRunner implements ApplicationRunner {

    private final BookBulkInsertRunnerProperties properties;
    private final BookBulkInsertRunner runner;
    private final List<BookBulkInserter> inserters;

    @Override
    public void run(ApplicationArguments args) {
        // Bulk insert 전략 선택
        BookBulkInserter inserter = inserters.stream()
                .filter(i -> i.name().equalsIgnoreCase(properties.method()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_BULK_INSERT_OPTIONS));

        // Bulk insert 옵션 생성
        BookBulkInsertOptions options = new BookBulkInsertOptions(
                properties.csvPath(),
                properties.batchSize(),
                properties.rowLimit()
        );

        // Bulk insert 실행
        BookBulkInsertResult result = properties.resetBeforeRun()
                ? runner.resetAndRun(inserter, options)
                : inserter.insert(options);

        log.info(
                "Book bulk insert 완료. method={}, batchSize={}, rowCount={}, elapsedMillis={}, rowsPerSecond={}",
                result.methodName(),
                result.batchSize(),
                result.rowCount(),
                result.elapsedMillis(),
                result.rowsPerSecond()
        );
    }
}
