package com.nhnacademy.springailibrarystudy.book.infrastructure.runner;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "book.bulk-insert.runner")
public record BookBulkInsertRunnerProperties(
        boolean enabled,
        String method,
        Path csvPath,
        int batchSize,
        int rowLimit,
        boolean resetBeforeRun
) {
}
