package com.nhnacademy.springailibrarystudy.feedback.application.dto;

import com.nhnacademy.springailibrarystudy.feedback.domain.FeedbackType;

public record SubmitFeedbackCommand(
        String userKey,
        String query,
        Long bookId,
        FeedbackType feedbackType
) {

    public SubmitFeedbackCommand {
        userKey = normalize(userKey);
        query = normalize(query);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
