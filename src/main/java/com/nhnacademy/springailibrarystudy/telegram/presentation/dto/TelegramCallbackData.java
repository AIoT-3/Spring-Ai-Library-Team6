package com.nhnacademy.springailibrarystudy.telegram.presentation.dto;

import com.nhnacademy.springailibrarystudy.feedback.domain.FeedbackType;
import java.util.Optional;
import org.springframework.util.StringUtils;

public record TelegramCallbackData(
        String contextId,
        Long bookId,
        FeedbackType feedbackType
) {
    public static final String FEEDBACK_PREFIX = "fb";

    // 직렬화: Telegram Callback Data의 길이 제한으로 FeedbackType 변환
    public static String format(String contextId, Long bookId, FeedbackType feedbackType) {
        if (!StringUtils.hasText(contextId) || bookId == null || feedbackType == null) {
            throw new IllegalArgumentException("callback data를 만들기 위한 값이 부족합니다.");
        }

        String type = switch (feedbackType) {
            case LIKE -> "L";
            case DISLIKE -> "D";
        };
        return String.join(":", FEEDBACK_PREFIX, contextId.trim(), bookId.toString(), type);
    }

    // 역직렬화
    public static Optional<TelegramCallbackData> parse(String callbackData) {
        // 입력 검증
        if (!StringUtils.hasText(callbackData)) {
            return Optional.empty();
        }
        String[] parts = callbackData.split(":");
        if (parts.length != 4 || !FEEDBACK_PREFIX.equals(parts[0])) {
            return Optional.empty();
        }

        // bookId와 feedbackType 변환
        try {
            Long bookId = Long.valueOf(parts[2]);
            FeedbackType type = switch (parts[3]) {
                case "L" -> FeedbackType.LIKE;
                case "D" -> FeedbackType.DISLIKE;
                default -> null;
            };

            return type == null || !StringUtils.hasText(parts[1])
                    ? Optional.empty()
                    : Optional.of(new TelegramCallbackData(parts[1].trim(), bookId, type));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
