package com.vincent.forexledger.model.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum SocialLoginProvider {
    FACEBOOK, GOOGLE;

    @JsonCreator
    public static SocialLoginProvider fromString(String key) {
        return Arrays.stream(values())
                .filter(value -> StringUtils.equalsIgnoreCase(key, value.name()))
                .findFirst()
                .orElse(null);
    }
}
