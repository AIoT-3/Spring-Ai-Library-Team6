package com.nhnacademy.springailibrarystudy.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * code 규칙
 * 1. {서비스명}_{에러번호} 형식으로 작성
 * 2. 에러번호는 001부터 시작하여 서비스별로 순차적으로 증가
 * 3. 에러코드의 의미는 다음과 같이 정의
 *    - COMMON: 공통 에러
 *    - BOOK: 도서 관련 에러
 *    - REVIEW: 리뷰 관련 에러
 * 4. HTTP status 매핑은 GlobalExceptionHandler에서
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_ISBN(
            "BOOK_001",
            "올바르지 않은 ISBN입니다."
    ),

    // 500 Internal Server Error
    INTERNAL_ERROR(
            "COMMON_001",
            "내부 서버 오류가 발생했습니다."
    );

    private final String code; // 로깅용 에러 코드
    private final String message; // 사용자에게 보여줄 메시지
}
