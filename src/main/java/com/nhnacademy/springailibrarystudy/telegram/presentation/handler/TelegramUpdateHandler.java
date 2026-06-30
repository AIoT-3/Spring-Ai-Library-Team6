package com.nhnacademy.springailibrarystudy.telegram.presentation.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramUpdateHandler {

    private final TelegramCallbackHandler callbackHandler;
    private final TelegramCommandHandler commandHandler;
    private final TelegramMessageHandler messageHandler;

    public List<BotApiMethod<?>> handle(Update update) {
        if (update == null) {
            return List.of();
        }

        // 콜백 쿼리 처리
        if (update.hasCallbackQuery()) {
            return callbackHandler.handle(update.getCallbackQuery());
        }

        if (!update.hasMessage()) {
            return List.of();
        }

        // 일반 메시지 처리
        Message message = update.getMessage();
        if (!message.hasText()) {
            return List.of();
        }

        // 명령어 처리
        if (commandHandler.supports(message.getText())) {
            return commandHandler.handle(message);
        }

        return messageHandler.handle(message);
    }
}
