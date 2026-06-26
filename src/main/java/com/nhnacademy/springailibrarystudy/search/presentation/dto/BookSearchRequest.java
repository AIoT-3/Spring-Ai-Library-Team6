package com.nhnacademy.springailibrarystudy.search.presentation.dto;

import com.nhnacademy.springailibrarystudy.search.domain.SearchType;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.ISBN;

public record BookSearchRequest(

        @Size(max = 100, message = "검색어는 100자 이하로 입력해주세요.")
        String query,

        @ISBN(type = ISBN.Type.ANY, message = "ISBN 형식이 올바르지 않습니다.")
        String isbn,

        String kdcCode,

        SearchType searchType,

        // [step-2 04.벡터검색]을 위한 필드 추가
        float[] vector
) {
}
