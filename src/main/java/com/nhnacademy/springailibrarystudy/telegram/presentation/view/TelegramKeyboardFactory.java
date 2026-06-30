package com.nhnacademy.springailibrarystudy.telegram.presentation.view;

import com.nhnacademy.springailibrarystudy.feedback.domain.FeedbackType;
import com.nhnacademy.springailibrarystudy.search.presentation.dto.BookSearchItemResponse;
import com.nhnacademy.springailibrarystudy.telegram.presentation.dto.TelegramCallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramKeyboardFactory {

    public InlineKeyboardMarkup createFeedbackKeyboard(
            String contextId,
            List<BookSearchItemResponse> sources
    ) {
        // 입력 검증
        if (!StringUtils.hasText(contextId) || sources == null || sources.isEmpty()) {
            return null;
        }

        // 각 책에 대한 좋아요/싫어요 버튼 생성
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            BookSearchItemResponse source = sources.get(i);
            if (source.id() == null) {
                log.debug("book id가 null이라 버튼 생성 스킵. index={}", i);
                continue;
            }

            int number = i + 1;

            InlineKeyboardButton like = new InlineKeyboardButton();
            like.setText("좋아요 " + number);
            like.setCallbackData(TelegramCallbackData.format(contextId, source.id(), FeedbackType.LIKE));

            InlineKeyboardButton dislike = new InlineKeyboardButton();
            dislike.setText("싫어요 " + number);
            dislike.setCallbackData(TelegramCallbackData.format(contextId, source.id(), FeedbackType.DISLIKE));

            rows.add(List.of(like, dislike));
        }

        if (rows.isEmpty()) {
            return null;
        }

        // 키보드 생성
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
