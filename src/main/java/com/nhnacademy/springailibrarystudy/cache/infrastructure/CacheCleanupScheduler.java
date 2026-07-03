package com.nhnacademy.springailibrarystudy.cache.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheCleanupScheduler {

    private final BookSearchCacheQueryRepository cacheQueryRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredCaches() {
        int deleted = cacheQueryRepository.deleteExpired();
        if (deleted > 0) {
            log.info("[캐시 정리] 만료 캐시 {}개 삭제", deleted);
        }
    }
}