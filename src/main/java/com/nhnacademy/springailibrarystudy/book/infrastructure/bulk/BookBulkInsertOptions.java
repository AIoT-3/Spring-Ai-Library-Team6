package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk;

import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import java.nio.file.Path;

public record BookBulkInsertOptions(
        Path csvPath,
        int batchSize,
        int rowLimit
) {

    public BookBulkInsertOptions {
        if (csvPath == null
                || batchSize <= 0
                || rowLimit < 0
        ) {
            throw new BusinessException(ErrorCode.INVALID_BULK_INSERT_OPTIONS);
        }
    }
}
