package com.nhnacademy.springailibrarystudy.telegram.presentation.view;

import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerResult;
import com.nhnacademy.springailibrarystudy.rag.application.dto.RagBookRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;

@Slf4j
@Component
public class TelegramMessageFormatter {

    // sendMessage의 공식 제한은 4096자
    private static final int MAX_MESSAGE_LENGTH = 3900;
    private static final int MAX_ANSWER_LENGTH = 1200;
    private static final int MAX_REASON_LENGTH = 400;
    // 텔레그램에 마크다운으로 나오는 게 맞는건지 보고도 잘 모르겠음;; 일단 더 깔끔하게 나와서 수정 보류
    private static final String MARKDOWN_V2_SPECIAL_CHARS = "_*[]()~`>#+-=|{}.!";

    public String parseMode() {
        return ParseMode.MARKDOWNV2;
    }

    public String formatRagAnswer(GenerateRagAnswerResult result) {
        if (result == null) {
            return "검색 결과를 만들지 못했습니다\\. 잠시 후 다시 시도해주세요\\.";
        }

        StringBuilder message = new StringBuilder();
        message.append("*도서 추천 결과*").append("\n\n");
        message.append(markdownText(result.answer(), MAX_ANSWER_LENGTH)).append("\n\n");

        // 결과가 없는 경우
        if (result.books().isEmpty()) {
            message.append("_검색 결과가 없습니다\\._");
            return message.toString();
        }

        // 결과가 있는 경우
        message.append("*검색 결과*").append("\n");
        for (int i = 0; i < result.books().size(); i++) {
            RagBookRecommendation item = result.books().get(i);
            String bookMessage = formatBook(i + 1, item);
            if (message.length() + bookMessage.length() > MAX_MESSAGE_LENGTH) {
                message.append("\n_일부 결과는 메시지 길이 제한으로 생략했습니다\\._").append("\n");
                break;
            }
            message.append(bookMessage);
        }
        message.append("\n_좋아요/싫어요 버튼을 눌러 피드백을 남겨주세요\\._");

        return message.toString();
    }

    public String formatStartMessage() {
        return """
                *도서 검색 봇*

                궁금한 책이나 주제를 일반 메시지로 보내면 관련 도서를 찾아드립니다\\.
                검색 결과 아래의 좋아요/싫어요 버튼으로 피드백을 남길 수 있습니다\\.
                """;
    }

    public String formatHelpMessage() {
        return """
                *사용 방법*

                1\\. 찾고 싶은 책 제목, 저자, 주제를 메시지로 입력하세요\\.
                2\\. 검색 결과를 확인하세요\\.
                3\\. 결과 아래 버튼으로 좋아요/싫어요 피드백을 남겨주세요\\.

                *명령어*
                `/start` \\- 시작 메시지
                `/help` \\- 도움말
                """;
    }

    public String formatUnsupportedCommandMessage() {
        return "지원하지 않는 명령어입니다\\. `/help` 를 입력해 사용법을 확인하세요\\.";
    }

    public String formatSearchErrorMessage() {
        return "검색 처리 중 오류가 발생했습니다\\. 잠시 후 다시 시도해주세요\\.";
    }

    private String formatBook(int number, RagBookRecommendation item) {
        StringBuilder message = new StringBuilder();
        message.append("\n")
                .append("*").append(number).append("\\. ")
                .append(markdownText(nullToDash(item.title()))).append("*").append("\n")
                .append("\\- 저자: ").append(markdownText(nullToDash(item.authorName()))).append("\n")
                .append("\\- 출판사: ").append(markdownText(nullToDash(item.publisherName()))).append("\n");

        if (item.recommendationReason() != null && !item.recommendationReason().isBlank()) {
            message.append("\\- 추천 이유: ")
                    .append(markdownText(item.recommendationReason().trim(), MAX_REASON_LENGTH))
                    .append("\n");
        }

        return message.toString();
    }

    private String markdownText(String value) {
        return escapeMarkdown(value);
    }

    private String markdownText(String value, int maxLength) {
        return escapeMarkdown(truncate(value, maxLength));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength) + "...";
    }

    private String escapeMarkdown(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '\\') {
                escaped.append("\\\\");
                continue;
            }
            if (MARKDOWN_V2_SPECIAL_CHARS.indexOf(current) >= 0) {
                escaped.append('\\');
            }
            escaped.append(current);
        }
        return escaped.toString();
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
