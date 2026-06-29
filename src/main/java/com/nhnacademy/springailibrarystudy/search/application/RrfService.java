package com.nhnacademy.springailibrarystudy.search.application;

import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RrfService {

    private static final int RRF_K = 60;

    public List<BookSearchItemResponse> fuse(List<BookSearchItemResponse> keywordSearchResponse, List<BookSearchItemResponse> vectorSearchResponse) {
        Map<Long, Double> rrfScores = new HashMap<>();
        Map<Long, BookSearchItemResponse> bookMap = new HashMap<>();

        for (int i = 0; i < keywordSearchResponse.size(); i++) {
            BookSearchItemResponse book = keywordSearchResponse.get(i);

            rrfScores.put(book.id(), rrfScores.getOrDefault(book.id(), 0.0) + 1.0 / (RRF_K + i + 1));
            bookMap.put(book.id(), book);
        }

        // 2. 벡터 검색 결과 처리 및 점수 계산
        for (int i = 0; i < vectorSearchResponse.size(); i++) {
            BookSearchItemResponse book = vectorSearchResponse.get(i);
            // 1 / (k + rank) 점수 누적
            rrfScores.put(book.id(), rrfScores.getOrDefault(book.id(), 0.0) + 1.0 / (RRF_K + i + 1));
            if (!bookMap.containsKey(book.id())) {
                bookMap.put(book.id(), book);
            } else {
                // 키워드 결과에 이미 존재하는 경우, 벡터 검색에서 추출된 유사도(Similarity) 정보를 보존하여 업데이트합니다.
                BookSearchItemResponse existing = bookMap.get(book.id());
                bookMap.put(book.id(), new BookSearchItemResponse(
                        existing.id(), existing.volumeTitle(), existing.title(),
                        existing.authorName(), existing.publisherName(), existing.publishedDate(),
                        existing.price(), existing.imageUrl(), book.similarity()
                ));
            }
        }

        // 3. 점수 기준 정렬 및 최종 응답 DTO 생성
        return rrfScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // 점수 내림차순 정렬
                .map(entry -> {
                    Long id = entry.getKey();
                    BookSearchItemResponse original = bookMap.get(id);
                    Double rrfScore = entry.getValue();
                    // 최종 필드에 RRF 점수를 포함하여 반환
                    return new BookSearchItemResponse(
                            original.id(), original.isbn13(), original.volumeTitle(), original.title(),
                            original.authorName(), original.publisherName(), original.publishedDate(),
                            original.price(), original.imageUrl(), original.description(), original.similarity(),
                            rrfScore
                    );
                })
                .toList();
    }
}
