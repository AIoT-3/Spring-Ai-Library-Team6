package com.nhnacademy.springailibrarystudy.search.application;

import com.nhnacademy.springailibrarystudy.book.infrastructure.persistence.BookQueryRepository;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchBooksByVectorUseCase {

    private final BookQueryRepository bookQueryRepository;
    private final EmbeddingModel embeddingModel;

    public Page<BookSearchItemResponse> search(BookSearchRequest request, Pageable pageable) {
        log.info("임베딩 요청: query='{}'", request.query());
        long start = System.currentTimeMillis();
        float[] vector = embeddingModel.embed(request.query());
        log.info("임베딩 완료: dimension={}, elapsed={}ms", vector.length, System.currentTimeMillis() - start);

        return bookQueryRepository.vectorSearch(pageable, vector);
    }
}
