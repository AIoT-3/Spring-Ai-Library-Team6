package com.nhnacademy.springailibrarystudy.book.application;

import com.nhnacademy.springailibrarystudy.book.domain.Book;
import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookRepository;
import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindBookUseCase {

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public Book findBookById(Long bookId) {
        if (bookId == null || bookId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_BOOK_ID);
        }

        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
    }
}
