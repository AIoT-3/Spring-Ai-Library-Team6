package com.nhnacademy.springailibrarystudy.personalization.domain;

import java.util.List;

public record UserPreference(
        String userKey,
        List<Long> likedBookIds,
        List<String> preferredKdcCodes,
        int feedbackCount
) {

    public UserPreference {
        userKey = userKey == null ? null : userKey.trim();
        likedBookIds = likedBookIds == null ? List.of() : List.copyOf(likedBookIds);
        preferredKdcCodes = preferredKdcCodes == null ? List.of() : List.copyOf(preferredKdcCodes);
        feedbackCount = Math.max(feedbackCount, 0);
    }
}
