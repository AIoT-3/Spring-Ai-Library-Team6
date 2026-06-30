package com.nhnacademy.springailibrarystudy.telegram.application;

import java.util.Optional;

public interface TelegramSearchContextStore {

    String save(TelegramSearchContext context);

    Optional<TelegramSearchContext> find(String contextId);

    void remove(String contextId);
}
