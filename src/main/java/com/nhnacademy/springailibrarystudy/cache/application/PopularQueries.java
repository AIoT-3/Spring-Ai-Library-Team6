package com.nhnacademy.springailibrarystudy.cache.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PopularQueries {

    JAVA("자바 프로그래밍 입문서 추천"),
    PYTHON("파이썬 데이터 분석 책"),
    ALGORITHM("알고리즘과 자료구조 교재"),
    SPRING("스프링 부트 실전 가이드"),
    DATABASE("데이터베이스 설계 입문"),
    NETWORK("컴퓨터 네트워크 기초"),
    AI("인공지능 머신러닝 입문서"),
    CS("컴퓨터 과학 기본서 추천");

    private final String query;
}