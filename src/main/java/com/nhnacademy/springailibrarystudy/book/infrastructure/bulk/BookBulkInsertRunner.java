package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BookBulkInsertRunner {

    private final List<BookBulkInserter> inserters;
    private final JdbcTemplate jdbcTemplate;

    public List<BookBulkInsertResult> runAll(BookBulkInsertOptions options) {
        return inserters.stream()
                .map(inserter -> resetAndRun(inserter, options))
                .toList();
    }

    public BookBulkInsertResult resetAndRun(BookBulkInserter inserter, BookBulkInsertOptions options) {
        resetBookTable();
        return inserter.insert(options);
    }

    private void resetBookTable() {
        jdbcTemplate.execute("truncate table book_embeddings, books");
        jdbcTemplate.execute("alter sequence book_sequence restart with 1");
        jdbcTemplate.execute("alter sequence book_embedding_sequence restart with 1");
    }
}
