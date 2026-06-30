package com.nhnacademy.springailibrarystudy.telegram.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramBotProperties(
        boolean enabled,
        String token,
        String username
) {
}
