package com.nhnacademy.springailibrarystudy.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// HTTP 요청에서 발생한 BusinessException을 ProblemDetail로 반환
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                statusOf(errorCode),
                errorCode.getMessage()
        );
        problemDetail.setTitle(errorCode.getCode());
        problemDetail.setProperty("code", errorCode.getCode());
        return problemDetail;
    }

    // 너무 많아지면 분리
    private HttpStatus statusOf(ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_ISBN,
                 INVALID_BOOK_CSV,
                 INVALID_BULK_INSERT_OPTIONS,
                 INVALID_EMBEDDING_GENERATION_OPTIONS,
                 INVALID_BOOK_ID -> HttpStatus.BAD_REQUEST;
            case BOOK_NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
