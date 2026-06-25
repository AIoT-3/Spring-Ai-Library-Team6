package com.nhnacademy.springailibrarystudy.book.infrastructure.persistence;

import com.nhnacademy.springailibrarystudy.book.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn13(String isbn13);
}
