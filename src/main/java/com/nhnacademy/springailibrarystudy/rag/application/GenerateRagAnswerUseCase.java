package com.nhnacademy.springailibrarystudy.rag.application;

import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerCommand;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerResult;
import com.nhnacademy.springailibrarystudy.search.application.SearchBooksUseCase;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenerateRagAnswerUseCase {

    private final SearchBooksUseCase searchBooksUseCase;

    public GenerateRagAnswerResult answer(GenerateRagAnswerCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        Page<BookSearchItemResponse> sources = searchBooksUseCase.search(
                toSearchRequest(command),
                PageRequest.of(0, command.topK())
        );

        return new GenerateRagAnswerResult(
                UUID.randomUUID().toString(),
                "RAG 답변 생성은 아직 구현 전입니다. 검색 결과를 기반으로 LLM 답변을 생성할 예정입니다.",
                sources.getContent(),
                false
        );
    }

    private BookSearchRequest toSearchRequest(GenerateRagAnswerCommand command) {
        return new BookSearchRequest(
                command.question(),
                null,
                null,
                command.searchType(),
                null
        );
    }
}
