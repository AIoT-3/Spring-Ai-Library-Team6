package com.nhnacademy.springailibrarystudy.feedback.application.dto;

import com.nhnacademy.springailibrarystudy.feedback.domain.FeedbackTargetType;
import com.nhnacademy.springailibrarystudy.feedback.domain.FeedbackType;

public record SubmitFeedbackCommand(
        String userKey,
        FeedbackTargetType targetType,
        String targetId,
        String question,
        FeedbackType feedbackType
) {

    public SubmitFeedbackCommand {
        userKey = normalize(userKey);
        targetId = normalize(targetId);
        question = normalize(question);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
