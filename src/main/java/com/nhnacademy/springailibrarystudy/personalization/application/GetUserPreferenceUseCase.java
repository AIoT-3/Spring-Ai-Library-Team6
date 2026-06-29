package com.nhnacademy.springailibrarystudy.personalization.application;

import java.util.List;

import com.nhnacademy.springailibrarystudy.personalization.domain.UserPreference;
import org.springframework.stereotype.Service;

@Service
public class GetUserPreferenceUseCase {

    public UserPreference findByUserKey(String userKey) {
        return new UserPreference(userKey, List.of(), List.of(), 0);
    }
}
