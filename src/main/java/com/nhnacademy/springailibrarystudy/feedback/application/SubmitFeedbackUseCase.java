package com.nhnacademy.springailibrarystudy.feedback.application;

import java.util.Objects;

import com.nhnacademy.springailibrarystudy.book.application.FindBookUseCase;
import com.nhnacademy.springailibrarystudy.book.domain.Book;
import com.nhnacademy.springailibrarystudy.feedback.application.dto.SubmitFeedbackCommand;
import com.nhnacademy.springailibrarystudy.feedback.application.dto.SubmitFeedbackResult;
import com.nhnacademy.springailibrarystudy.feedback.domain.SearchFeedback;
import com.nhnacademy.springailibrarystudy.feedback.infrastructure.SearchFeedbackRepository;
import com.nhnacademy.springailibrarystudy.global.exception.BusinessException;
import com.nhnacademy.springailibrarystudy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SubmitFeedbackUseCase {

    private final SearchFeedbackRepository searchFeedbackRepository;
    private final FindBookUseCase findBookUseCase;

    @Transactional
    @CacheEvict(cacheNames = "userPreferenceVectors", key = "#command.userKey()")
    public SubmitFeedbackResult submit(SubmitFeedbackCommand command) {
        // 입력 검증
        Objects.requireNonNull(command, "command는 null일 수 없습니다.");
        if (!StringUtils.hasText(command.userKey())
                || !StringUtils.hasText(command.query())
                || command.bookId() == null
                || command.feedbackType() == null
        ) {
            throw new BusinessException(ErrorCode.INVALID_FEEDBACK_COMMAND);
        }

        // 도서 조회
        Book book = findBookUseCase.findBookById(command.bookId());

        // 피드백 생성 및 저장
        SearchFeedback feedback = SearchFeedback.builder()
                .book(book)
                .userKey(command.userKey())
                .query(command.query())
                .feedbackType(command.feedbackType())
                .build();
        searchFeedbackRepository.save(feedback);

        return new SubmitFeedbackResult(
                true,
                command.userKey(),
                command.query(),
                command.bookId(),
                command.feedbackType(),
                "피드백이 저장되었습니다."
        );
    }
}
