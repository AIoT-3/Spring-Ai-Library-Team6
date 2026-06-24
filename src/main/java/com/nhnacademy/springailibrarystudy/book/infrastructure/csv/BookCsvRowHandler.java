package com.nhnacademy.springailibrarystudy.book.infrastructure.csv;

@FunctionalInterface
public interface BookCsvRowHandler {
    boolean handle(BookCsvRow row);
}
