package com.nhnacademy.springailibrarystudy.telegram.presentation.handler;

import com.nhnacademy.springailibrarystudy.telegram.presentation.view.TelegramMessageFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramCommandHandler {

    private final TelegramMessageFormatter messageFormatter;

    public boolean supports(String text) {
        return text != null && text.startsWith("/");
    }

    public List<BotApiMethod<?>> handle(Message message) {
        if (message == null) {
            return List.of();
        }

        String command = normalizeCommand(message.getText());
        String text = switch (command) {
            case "/start" -> messageFormatter.formatStartMessage();
            case "/help" -> messageFormatter.formatHelpMessage();
            default -> "지원하지 않는 명령어입니다. /help 를 입력해 사용법을 확인하세요.";
        };

        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(message.getChatId()));
        response.setText(text);
        return List.of(response);
    }

    private String normalizeCommand(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text.trim().split("\\s+", 2)[0].toLowerCase();
    }
}
