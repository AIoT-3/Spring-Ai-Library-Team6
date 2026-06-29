package com.nhnacademy.springailibrarystudy.feedback.application;

import java.util.Objects;

import com.nhnacademy.springailibrarystudy.feedback.application.dto.SubmitFeedbackCommand;
import com.nhnacademy.springailibrarystudy.feedback.application.dto.SubmitFeedbackResult;
import org.springframework.stereotype.Service;

@Service
public class SubmitFeedbackUseCase {

    public SubmitFeedbackResult submit(SubmitFeedbackCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        return new SubmitFeedbackResult(
                true,
                command.userKey(),
                command.query(),
                command.bookId(),
                command.feedbackType(),
                "피드백 저장 구현 전입니다. 현재는 팀 연동용 계약만 제공합니다."
        );
    }
}
