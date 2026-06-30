package com.nhnacademy.springailibrarystudy.telegram.presentation.handler;

import com.nhnacademy.springailibrarystudy.rag.application.SearchBooksRagUseCase;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerCommand;
import com.nhnacademy.springailibrarystudy.rag.application.dto.GenerateRagAnswerResult;
import com.nhnacademy.springailibrarystudy.telegram.application.TelegramSearchContext;
import com.nhnacademy.springailibrarystudy.telegram.application.TelegramSearchContextStore;
import com.nhnacademy.springailibrarystudy.telegram.presentation.view.TelegramKeyboardFactory;
import com.nhnacademy.springailibrarystudy.telegram.presentation.view.TelegramMessageFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramMessageHandler {

    private final SearchBooksRagUseCase searchBooksRagUseCase;
    private final TelegramSearchContextStore searchContextStore;
    private final TelegramMessageFormatter messageFormatter;
    private final TelegramKeyboardFactory keyboardFactory;

    public List<BotApiMethod<?>> handle(Message message) {
        // 입력 검증
        if (!StringUtils.hasText(message.getText())) {
            return List.of();
        }

        // 질문과 사용자 키 추출
        String question = message.getText().trim();
        String userKey = toUserKey(message);

        try {
            // RAG 답변 생성
            GenerateRagAnswerResult result = searchBooksRagUseCase.answer(
                    new GenerateRagAnswerCommand(question, userKey, 10, 5)
            );

            // 답변 메시지 생성
            SendMessage response = createMessage(message, messageFormatter.formatRagAnswer(result));
            if (!result.books().isEmpty()) {
                // 캐시 저장 및 피드백 버튼 생성
                String contextId = searchContextStore.save(new TelegramSearchContext(userKey, question));
                response.setReplyMarkup(keyboardFactory.createFeedbackKeyboard(contextId, result.books()));
            }

            return List.of(response);
        } catch (RuntimeException e) {
            log.warn("telegram search message를 처리하는 중 오류가 발생했습니다. chatId={}", message.getChatId(), e);
            return List.of(createMessage(message, messageFormatter.formatSearchErrorMessage()));
        }
    }

    private SendMessage createMessage(Message message, String text) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(message.getChatId()));
        response.setText(text);
        response.setParseMode(messageFormatter.parseMode());
        return response;
    }

    private String toUserKey(Message message) {
        if (message.getFrom() != null) {
            return "telegram:user:" + message.getFrom().getId();
        }

        return "telegram:chat:" + message.getChatId();
    }
}
