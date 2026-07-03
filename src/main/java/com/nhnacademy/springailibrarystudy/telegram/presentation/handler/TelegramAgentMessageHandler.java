package com.nhnacademy.springailibrarystudy.telegram.presentation.handler;

import com.nhnacademy.springailibrarystudy.ai.agent.application.AiLibraryAssistantService;
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
public class TelegramAgentMessageHandler {

    private final AiLibraryAssistantService aiLibraryAssistantService;
    private final TelegramMessageFormatter messageFormatter;

    public List<BotApiMethod<?>> handle(Message message) {
        if (message == null || !StringUtils.hasText(message.getText())) {
            return List.of();
        }

        String text;
        try {
            String answer = aiLibraryAssistantService.ask(message.getText().trim());
            text = messageFormatter.formatAiAnswer(answer);
        } catch (RuntimeException e) {
            log.warn("telegram agent message를 처리하는 중 오류가 발생했습니다. chatId={}", message.getChatId(), e);
            text = messageFormatter.formatAiErrorMessage();
        }

        return List.of(buildMessage(message, text));
    }

    private SendMessage buildMessage(Message message, String text) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(message.getChatId()));
        response.setText(text);
        response.setParseMode(messageFormatter.parseMode());
        return response;
    }
}
