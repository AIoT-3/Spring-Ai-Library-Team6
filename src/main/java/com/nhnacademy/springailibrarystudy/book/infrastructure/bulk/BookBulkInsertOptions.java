package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk;

import java.nio.file.Path;

public record BookBulkInsertOptions(
        Path csvPath,
        int batchSize,
        int rowLimit
) {
}
