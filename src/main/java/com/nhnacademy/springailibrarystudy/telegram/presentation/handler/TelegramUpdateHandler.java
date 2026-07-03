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
    // 모든 메시지를 에이전트에게 위임하도록 여기서 라우팅만 교체한다.
    private final TelegramAgentMessageHandler agentMessageHandler;

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

        // 모든 메시지를 에이전트에게 위임 (명령어/일반 메시지 구분 없음)
        Message message = update.getMessage();
        if (!message.hasText()) {
            return List.of();
        }

        return agentMessageHandler.handle(message);
    }
}
