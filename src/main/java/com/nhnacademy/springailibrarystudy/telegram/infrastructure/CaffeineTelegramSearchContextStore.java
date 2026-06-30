package com.nhnacademy.springailibrarystudy.telegram.infrastructure;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nhnacademy.springailibrarystudy.telegram.application.TelegramSearchContext;
import com.nhnacademy.springailibrarystudy.telegram.application.TelegramSearchContextStore;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// 굳이 Redis일 필요는 없는 것 같아서, 일단 Caffeine
@Component
public class CaffeineTelegramSearchContextStore implements TelegramSearchContextStore {

    private static final Duration EXPIRE_AFTER_WRITE = Duration.ofMinutes(10);
    private static final long MAXIMUM_SIZE = 10_000L;

    private final Cache<String, TelegramSearchContext> cache = Caffeine.newBuilder()
            .expireAfterWrite(EXPIRE_AFTER_WRITE)
            .maximumSize(MAXIMUM_SIZE)
            .build();

    @Override
    public String save(TelegramSearchContext context) {
        Objects.requireNonNull(context, "context는 null일 수 없습니다.");

        String contextId = UUID.randomUUID().toString();
        cache.put(contextId, context);

        return contextId;
    }

    @Override
    public Optional<TelegramSearchContext> find(String contextId) {
        return Optional.ofNullable(normalize(contextId))
                .map(cache::getIfPresent);
    }

    @Override
    public void remove(String contextId) {
        Optional.ofNullable(normalize(contextId))
                .ifPresent(cache::invalidate);
    }

    private String normalize(String contextId) {
        if (!StringUtils.hasText(contextId)) {
            return null;
        }

        return contextId.trim();
    }
}
