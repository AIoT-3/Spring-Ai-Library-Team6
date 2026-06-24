package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk;

public record BookBulkInsertResult(
        String methodName,
        int batchSize,
        int rowCount,
        long elapsedMillis,
        double rowsPerSecond
) {
    public static BookBulkInsertResult of(
            String methodName,
            int batchSize,
            int rowCount,
            long elapsedMillis
    ) {
        return new BookBulkInsertResult(
                methodName,
                batchSize,
                rowCount,
                elapsedMillis,
                elapsedMillis == 0 ? 0.0 : rowCount * 1000.0 / elapsedMillis
        );
    }
}
