package com.nhnacademy.springailibrarystudy.rag.infrastructure;

import com.nhnacademy.springailibrarystudy.personalization.application.RerankBookSearchResultsUseCase;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookCandidate;
import com.nhnacademy.springailibrarystudy.review.application.ReviewInfoEnricher;
import com.nhnacademy.springailibrarystudy.search.application.SearchBooksHybridUseCase;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

// FIXME: infrastructure 보다는 application 쪽으로 옮기는 것 고려 (외부 API 호출이 아닌 내부 UseCase 호출이라서)
// FIXME: 이제는 reranking까지 끼여있어서 RagBookCandidateProvider 같은 이름이 더 적절할 수도 있음
@Slf4j
@Component
@RequiredArgsConstructor
public class HybridBookCandidateSearcher {

    private final SearchBooksHybridUseCase searchBooksHybridUseCase;
    private final RerankBookSearchResultsUseCase rerankBookSearchResultsUseCase;
    private final ReviewInfoEnricher reviewInfoEnricher;

    public List<RagBookCandidate> search(String query, int candidateTopK, String userKey) {

        // 하이브리드 검색
        List<BookSearchItemResponse> searchedBooks = searchBooksHybridUseCase
                .search(BookSearchRequest.hybrid(query),
                        PageRequest.of(0, candidateTopK))
                .getContent();

        // 개인화 기반 재정렬
        List<BookSearchItemResponse> rerankedBooks =
                rerankBookSearchResultsUseCase.rerank(searchedBooks, userKey);

        //enrich를 거치기 위함
        List<BookSearchItemResponse> enrichedResults = reviewInfoEnricher.enrich(rerankedBooks);

        List<RagBookCandidate> candidates = enrichedResults.stream()
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
                item.rrfScore(),
                item.averageRating(),
                item.reviewCount(),
                item.reviewSummary()
        );
    }
}
