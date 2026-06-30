package com.nhnacademy.springailibrarystudy.feedback.application.dto;

import com.nhnacademy.springailibrarystudy.feedback.domain.FeedbackType;

public record SubmitFeedbackResult(
        boolean accepted,
        String userKey,
        String query,
        Long bookId,
        FeedbackType feedbackType,
        String message
) {
}
