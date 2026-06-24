package com.nhnacademy.springailibrarystudy.book.infrastructure.bulk;

public interface BookBulkInserter {

    String name();

    BookBulkInsertResult insert(BookBulkInsertOptions options);
}
