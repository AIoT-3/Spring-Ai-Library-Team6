package com.nhnacademy.springailibrarystudy.telegram.presentation.bot;

import com.nhnacademy.springailibrarystudy.telegram.infrastructure.config.TelegramBotProperties;
import com.nhnacademy.springailibrarystudy.telegram.presentation.handler.TelegramUpdateHandler;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class LibraryTelegramBot extends TelegramLongPollingBot {

    private final TelegramBotProperties properties;
    private final TelegramUpdateHandler updateHandler;

    public LibraryTelegramBot(TelegramBotProperties properties, TelegramUpdateHandler updateHandler) {
        super(properties.token());
        this.properties = properties;
        this.updateHandler = updateHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        List<BotApiMethod<?>> methods;
        try {
            methods = updateHandler.handle(update);
        } catch (RuntimeException e) {
            log.warn("telegram update 처리 실패", e);
            return;
        }

        for (BotApiMethod<?> method : methods) {
            try {
                execute(method);
            } catch (TelegramApiException e) {
                log.warn("telegram method 실행 실패. method={}", method.getMethod(), e);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return properties.username();
    }
}
