package com.nhnacademy.springailibrarystudy.review.application;

import com.nhnacademy.springailibrarystudy.review.domain.BookReview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * - summarizeAll        : 전체 요약 (Map-Reduce)
 * - summarizeIncremental: 누적 요약 (새 리뷰만 Map → 기존과 Merge)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewSummarizer {

    private final ChatModel chatModel;

    private static final int CHUNK_SIZE = 10;       // 한 번에 묶어 요약할 리뷰 수

    /** 전체 요약: Map(청크별 요약) → Reduce(합치기) */
    public String summarizeAll(List<BookReview> reviews) {
        List<String> partials = new ArrayList<>();
        for (List<BookReview> chunk : partition(reviews, CHUNK_SIZE)) {
            partials.add(summarizeChunk(chunk));
        }
        return reduce(partials);
    }

    /** 누적 요약: 새 리뷰만 Map → 기존 요약과 Merge */
    public String summarizeIncremental(String existingSummary, List<BookReview> newReviews) {
        List<String> partials = new ArrayList<>();
        for (List<BookReview> chunk : partition(newReviews, CHUNK_SIZE)) {
            partials.add(summarizeChunk(chunk));
        }
        String newSummary = reduce(partials);
        return merge(existingSummary, newSummary);
    }

    private String summarizeChunk(List<BookReview> chunk) {
        StringBuilder sb = new StringBuilder();
        for (BookReview r : chunk) {
            sb.append("- (별점 ").append(r.getRating()).append(") ")
                    .append(r.getContent()).append("\n");
        }
        String prompt = """
                다음은 한 도서에 대한 사용자 리뷰 묶음입니다.
                장점, 단점, 추천 대상을 중심으로 3~5문장으로 요약해주세요.

                리뷰:
                %s
                """.formatted(sb);
        return chatModel.call(prompt);
    }

    private String reduce(List<String> partials) {
        if (partials.isEmpty()) {
            return "";
        }
        if (partials.size() == 1) {
            return partials.get(0);
        }
        String joined = String.join("\n---\n", partials);
        String prompt = """
                아래는 같은 도서에 대한 여러 부분 요약입니다.
                이를 하나로 통합해 장점/단점/총평으로 정리해주세요.

                부분 요약들:
                %s

                출력 형식:
                장점: ...
                단점: ...
                총평: ...
                """.formatted(joined);
        return chatModel.call(prompt);
    }

    private String merge(String existingSummary, String newSummary) {
        if (existingSummary == null || existingSummary.isBlank()) {
            return newSummary;
        }
        String prompt = """
                기존 요약:
                %s

                새로운 리뷰 요약본:
                %s

                위 두 내용을 통합해 전체 리뷰를 종합한 '최종 요약'을 작성해주세요.
                기존 요약의 핵심은 유지하면서 새로운 정보를 반영하세요.

                출력 형식:
                장점: ...
                단점: ...
                총평: ...
                """.formatted(existingSummary, newSummary);
        return chatModel.call(prompt);
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }
}