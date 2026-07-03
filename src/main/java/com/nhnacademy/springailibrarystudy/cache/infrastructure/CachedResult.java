package com.nhnacademy.springailibrarystudy.cache.infrastructure;

/**
 * 캐시 조회 결과.
 * id 는 접근 정보(lastAccessedAt, accessCount) 갱신 시 필요해서 같이 담는다.
 */
public record CachedResult(
        Long id,
        String result,
        Double similarity
) {
}