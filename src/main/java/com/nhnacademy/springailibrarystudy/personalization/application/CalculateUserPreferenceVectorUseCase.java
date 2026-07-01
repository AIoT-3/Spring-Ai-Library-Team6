package com.nhnacademy.springailibrarystudy.personalization.application;

import com.nhnacademy.springailibrarystudy.personalization.domain.UserPreferenceVector;
import com.nhnacademy.springailibrarystudy.personalization.infrastructure.PersonalizationQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CalculateUserPreferenceVectorUseCase {

    private static final int RECENT_FEEDBACK_LIMIT = 20;

    private final PersonalizationQueryRepository personalizationQueryRepository;

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "userPreferenceVectors",
            key = "#userKey",
            // 캐싱하지 않을 조건
            unless = "#result == null || !#result.personalizable()"
    )
    public UserPreferenceVector calculate(String userKey) {
        if (!StringUtils.hasText(userKey)) {
            return null;
        }

        return personalizationQueryRepository.findUserPreferenceVector(
                userKey.trim(),
                RECENT_FEEDBACK_LIMIT
        );
    }
}
