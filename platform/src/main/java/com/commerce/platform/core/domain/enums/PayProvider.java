package com.commerce.platform.core.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PayProvider {
    // 카드
    SHIN_HAN("shinHan"),
    KB("kb"),
    NH("nh"),
    HYUNDAI("hyundai"),
    SAMSUNG("samsung"),
    BC("bc"),
    LOTTE("lotte"),
    HANA("hana"),

    // 휴대폰
    LG("lg"),
    KT("kt"),
    SKT("skt")
    ;

    private final String value;

    public static PayProvider getPayProviderByValue(String value) {
        return Arrays.stream(PayProvider.values())
                .filter(p -> p.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 결제사: " + value));
    }
}
