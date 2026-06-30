package com.nhnacademy.springailibrarystudy.telegram.presentation.view;

import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerResult;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// TODO: 일단 일반 텍스트로 함. 시간 나면 Markdown 형식으로..?
@Slf4j
@Component
public class TelegramMessageFormatter {

    // 공식 제한은 4096자라, 안전하게 3500자로 제한 (변경 가능)
    private static final int MAX_MESSAGE_LENGTH = 3500;

    public String formatRagAnswer(GenerateRagAnswerResult result) {
        if (result == null) {
            return "검색 결과를 만들지 못했습니다. 잠시 후 다시 시도해주세요.";
        }

        StringBuilder message = new StringBuilder();
        message.append(result.answer()).append("\n\n");

        // 결과가 없는 경우
        if (result.books().isEmpty()) {
            message.append("검색 결과가 없습니다.");
            return message.toString();
        }

        // 결과가 있는 경우
        message.append("검색 결과\n");
        for (int i = 0; i < result.books().size(); i++) {
            RagBookRecommendation item = result.books().get(i);
            message.append(i + 1).append(". ")
                    .append(nullToDash(item.title())).append(" / ")
                    .append(nullToDash(item.authorName())).append(" / ")
                    .append(nullToDash(item.publisherName())).append("\n");
            if (item.recommendationReason() != null && !item.recommendationReason().isBlank()) {
                message.append("   추천 이유: ")
                        .append(item.recommendationReason().trim())
                        .append("\n");
            }
        }
        message.append("\n좋아요/싫어요 버튼을 눌러 피드백을 남겨주세요.");

        // 메시지 길이 제한 적용
        return message.toString().length() > MAX_MESSAGE_LENGTH
                ? message.substring(0, MAX_MESSAGE_LENGTH) + "\n..."
                : message.toString();
    }

    public String formatStartMessage() {
        return """
                안녕하세요. 도서 검색 봇입니다.

                궁금한 책이나 주제를 일반 메시지로 보내면 관련 도서를 찾아드립니다.
                검색 결과 아래의 좋아요/싫어요 버튼으로 피드백을 남길 수 있습니다.
                """;
    }

    public String formatHelpMessage() {
        return """
                사용 방법

                1. 찾고 싶은 책 제목, 저자, 주제를 메시지로 입력하세요.
                2. 검색 결과를 확인하세요.
                3. 결과 아래 버튼으로 좋아요/싫어요 피드백을 남겨주세요.

                명령어
                /start - 시작 메시지
                /help - 도움말
                """;
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
