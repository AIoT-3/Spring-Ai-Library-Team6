package com.nhnacademy.springailibrarystudy.book.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookEmbeddingSourceTextBuilder {
    /*
    도서 임베딩을 위한 source text 생성 책임
    1. 어떤 필드를 source text로 사용할지 결정
    2. null/blank 제거
    3. 전처리 (특수문자 제거, 지은이 같은 노이즈 제거 등)
    4. 너무 긴 경우 일부만 사용할지 결정
    5. 최종적으로 source text를 반환
     */

}
