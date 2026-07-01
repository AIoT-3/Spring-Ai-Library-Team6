package com.nhnacademy.springailibrarystudy.personalization.application;

import com.nhnacademy.springailibrarystudy.personalization.domain.CandidatePreferenceSimilarity;
import com.nhnacademy.springailibrarystudy.personalization.domain.PersonalizationScoringPolicy;
import com.nhnacademy.springailibrarystudy.personalization.domain.PersonalizedBookScore;
import com.nhnacademy.springailibrarystudy.personalization.domain.UserPreferenceVector;
import com.nhnacademy.springailibrarystudy.personalization.infrastructure.PersonalizationQueryRepository;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RerankBookSearchResultsUseCase {

    private final CalculateUserPreferenceVectorUseCase calculateUserPreferenceVectorUseCase;
    private final PersonalizationQueryRepository personalizationQueryRepository;

    private final PersonalizationScoringPolicy scoringPolicy = PersonalizationScoringPolicy.defaults();

    @Transactional(readOnly = true)
    public List<BookSearchItemResponse> rerank(List<BookSearchItemResponse> results, String userKey) {
        // 개인화 점수 계산이 불가능한 경우, 기존 결과를 그대로 반환
        if (results == null || results.isEmpty() || !StringUtils.hasText(userKey)) {
            return results;
        }

        // 개인화 점수 계산을 위한 사용자 선호 벡터 조회
        UserPreferenceVector preference = calculateUserPreferenceVectorUseCase.calculate(userKey);
        if (preference == null) {
            return results;
        }

        if (!preference.personalizable()) {
            return results;
        }

        // RRF 점수 정규화를 위한 최대 RRF 점수 계산
        double maxRrfScore = results.stream()
                .map(BookSearchItemResponse::rrfScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
        if (maxRrfScore <= 0.0) {
            return results;
        }

        // 사용자 선호 벡터와 후보 책들의 유사도 계산
        List<Long> bookIds = results.stream()
                .map(BookSearchItemResponse::id)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, CandidatePreferenceSimilarity> similarityByBookId =
                personalizationQueryRepository.findCandidateSimilarities(
                                bookIds,
                                preference.likeVector(),
                                preference.dislikeVector()
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                CandidatePreferenceSimilarity::bookId,
                                Function.identity(),
                                (left, right) -> left
                        ));

        // 점수 계산용 객체 생성
        Map<Long, PersonalizedBookScore> scoreByBookId = results.stream()
                .filter(item -> item.id() != null)
                .collect(Collectors.toMap(
                        BookSearchItemResponse::id,
                        item -> toPersonalizedScore(item, similarityByBookId.get(item.id()), maxRrfScore),
                        (left, right) -> left
                ));

        // 최종 점수 계산 및 정렬
        // 현재: 벡터 연산은 DB, 점수 정책은 Java
        // 혹시: 후보 수가 커져 Java 정렬 비용이나 네트워크 비용이 의미 있어지면,
        // normalizedRrf와 weight를 SQL에 넘겨 DB에서 final_score 계산 및 정렬까지 수행 가능
        List<BookSearchItemResponse> rerankedResults = results.stream()
                .sorted(Comparator.<BookSearchItemResponse>comparingDouble(
                        item -> finalScore(scoreByBookId.get(item.id()))
                ).reversed())
                .toList();

        if (log.isDebugEnabled()) {
            List<Long> rerankedBookIds = rerankedResults.stream()
                    .map(BookSearchItemResponse::id)
                    .filter(Objects::nonNull)
                    .toList();
            log.debug(
                    "개인화 재정렬 적용: candidates={}, likeCount={}, dislikeCount={}, similarities={}",
                    results.size(),
                    preference.likeCount(),
                    preference.dislikeCount(),
                    similarityByBookId.size()
            );
            log.debug("beforeIds={}", bookIds);
            log.debug("afterIds ={}", rerankedBookIds);
        }

        return rerankedResults;
    }

    private PersonalizedBookScore toPersonalizedScore(
            BookSearchItemResponse item,
            CandidatePreferenceSimilarity similarity,
            double maxRrfScore
    ) {
        // RRF 점수는 코사인 유사도보다 스케일이 작아서,
        // 현재 후보군의 최대 RRF 점수를 기준으로 0~1 범위로 정규화
        double normalizedRrfScore = item.rrfScore() == null ? 0.0 : item.rrfScore() / maxRrfScore;
        double likeSimilarity = similarity == null ? 0.0 : similarity.likeSignal();
        double dislikeSimilarity = similarity == null ? 0.0 : similarity.dislikeSignal();

        return new PersonalizedBookScore(
                item.id(),
                normalizedRrfScore,
                likeSimilarity,
                dislikeSimilarity
        );
    }

    private double finalScore(PersonalizedBookScore score) {
        if (score == null) {
            return 0.0;
        }

        return score.finalScore(scoringPolicy);
    }
}
