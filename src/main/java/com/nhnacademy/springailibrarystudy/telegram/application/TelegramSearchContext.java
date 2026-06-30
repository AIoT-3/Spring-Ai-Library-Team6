package com.nhnacademy.springailibrarystudy.telegram.application;

public record TelegramSearchContext(
        String userKey,
        String query
) {
}