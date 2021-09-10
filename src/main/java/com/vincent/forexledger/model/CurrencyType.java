package com.vincent.forexledger.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum CurrencyType {
    USD, CNY, JPY, EUR, HKD, AUD, ZAR, CAD, GBP, SGD, CHF, NZD, SEK, THB;

    public static CurrencyType fromString(String typeStr) {
        return Arrays.stream(values())
                .filter(type -> StringUtils.equalsIgnoreCase(type.name(), typeStr))
                .findFirst()
                .orElse(null);
    }
}
