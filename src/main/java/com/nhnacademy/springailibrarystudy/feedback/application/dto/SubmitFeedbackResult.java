package com.nhnacademy.springailibrarystudy.feedback.application.dto;

import com.nhnacademy.springailibrarystudy.feedback.domain.FeedbackTargetType;
import com.nhnacademy.springailibrarystudy.feedback.domain.FeedbackType;

public record SubmitFeedbackResult(
        boolean accepted,
        String userKey,
        FeedbackTargetType targetType,
        String targetId,
        FeedbackType feedbackType,
        String message
) {
}
