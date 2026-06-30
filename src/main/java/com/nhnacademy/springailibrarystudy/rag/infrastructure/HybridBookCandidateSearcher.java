package com.nhnacademy.springailibrarystudy.rag.infrastructure;

import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import com.nhnacademy.springailibrarystudy.search.application.SearchBooksHybridUseCase;
import com.nhnacademy.springailibrarystudy.search.domain.SearchType;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HybridBookCandidateSearcher {

    private final SearchBooksHybridUseCase searchBooksHybridUseCase;

    public List<RagBookCandidate> search(String query, int candidateTopK) {
        BookSearchRequest request = new BookSearchRequest(query, null, null, SearchType.HYBRID, null);

        List<RagBookCandidate> candidates = searchBooksHybridUseCase.search(request, PageRequest.of(0, candidateTopK))
                .getContent().stream()
                .map(HybridBookCandidateSearcher::toCandidate)
                .toList();

        log.info("RAG 후보 검색 완료: {}건 (query='{}', topK={})", candidates.size(), query, candidateTopK);
        if (log.isDebugEnabled()) {
            candidates.forEach(candidate -> log.debug(
                    "  RAG 후보 id={}, title='{}', similarity={}, rrfScore={}",
                    candidate.id(), candidate.title(), candidate.similarity(), candidate.rrfScore()));
        }

        return candidates;
    }

    private static RagBookCandidate toCandidate(BookSearchItemResponse item) {
        return new RagBookCandidate(
                item.id(),
                item.isbn13(),
                item.title(),
                item.authorName(),
                item.publisherName(),
                item.description(),
                item.imageUrl(),
                item.similarity(),
                item.rrfScore()
        );
    }
}
