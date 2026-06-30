package com.nhnacademy.springailibrarystudy.telegram.presentation.handler;

import com.nhnacademy.springailibrarystudy.feedback.application.SubmitFeedbackUseCase;
import com.nhnacademy.springailibrarystudy.feedback.application.dto.SubmitFeedbackCommand;
import com.nhnacademy.springailibrarystudy.telegram.application.TelegramSearchContext;
import com.nhnacademy.springailibrarystudy.telegram.application.TelegramSearchContextStore;
import com.nhnacademy.springailibrarystudy.telegram.presentation.dto.TelegramCallbackData;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramCallbackHandler {

    private final SubmitFeedbackUseCase submitFeedbackUseCase;
    private final TelegramSearchContextStore searchContextStore;

    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        // 입력 검증
        if (callbackQuery == null) {
            return List.of();
        }

        // 콜백 데이터 파싱
        Optional<TelegramCallbackData> parsed = TelegramCallbackData.parse(callbackQuery.getData());
        if (parsed.isEmpty()) {
            return List.of(answer(callbackQuery.getId(), "잘못된 피드백 요청입니다."));
        }

        // 검색 컨텍스트 조회
        // (callback data의 길이 제한 때문에 필요한 정보를 모두 담을 수 없으므로, contextId를 통해 검색 컨텍스트를 조회)
        TelegramCallbackData data = parsed.get();
        Optional<TelegramSearchContext> context = searchContextStore.find(data.contextId());
        if (context.isEmpty()) {
            return List.of(answer(callbackQuery.getId(), "검색 정보가 만료되었습니다. 다시 검색해주세요."));
        }

        try {
            submitFeedbackUseCase.submit(new SubmitFeedbackCommand(
                    context.get().userKey(),
                    context.get().query(),
                    data.bookId(),
                    data.feedbackType()
            ));

            return List.of(answer(callbackQuery.getId(), "피드백이 저장되었습니다."));
        } catch (RuntimeException e) {
            log.warn("telegram feedback 처리 중 오류가 발생했습니다. contextId={}, bookId={}",
                    data.contextId(),
                    data.bookId(),
                    e
            );
            return List.of(answer(callbackQuery.getId(), "피드백 처리 중 오류가 발생했습니다."));
        }
    }

    private AnswerCallbackQuery answer(String callbackQueryId, String text) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQueryId);
        answer.setText(text);
        answer.setShowAlert(false);
        return answer;
    }
}
