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
public class TelegramCommandHandler {

    private final TelegramMessageFormatter messageFormatter;
    private final AiLibraryAssistantService aiLibraryAssistantService;

    public boolean supports(String text) {
        return text != null && text.startsWith("/");
    }

    public List<BotApiMethod<?>> handle(Message message) {
        if (message == null) {
            return List.of();
        }

        String command = normalizeCommand(message.getText());
        String text;
        try {
            // Step 7: /ai 명령은 LLM이 도구를 스스로 호출하는 새 오케스트레이션 경로. 궁극적으로 telegram은 chatbot으로 기능하게 될 예정이므로 해당 부분이 삭제 또는 더이상 사용 안할 수 있음.
            text = switch (command) {
                case "/ai" -> handleAiCommand(message.getText());
                case "/start" -> messageFormatter.formatStartMessage();
                case "/help" -> messageFormatter.formatHelpMessage();
                default -> messageFormatter.formatUnsupportedCommandMessage();
            };
        } catch (RuntimeException e) {
            log.warn("telegram command를 처리하는 중 오류가 발생했습니다. chatId={}", message.getChatId(), e);
            text = messageFormatter.formatAiErrorMessage();
        }

        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(message.getChatId()));
        response.setText(text);
        response.setParseMode(messageFormatter.parseMode());
        return List.of(response);
    }

    private String handleAiCommand(String rawText) {
        String question = extractArgument(rawText);
        if (!StringUtils.hasText(question)) {
            return messageFormatter.formatAiUsageHintMessage();
        }

        String answer = aiLibraryAssistantService.ask(question);
        return messageFormatter.formatAiAnswer(answer);
    }

    private String extractArgument(String text) {
        String[] parts = text.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }

    private String normalizeCommand(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text.trim().split("\\s+", 2)[0].toLowerCase();
    }
}
