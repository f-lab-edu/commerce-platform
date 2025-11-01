package com.commerce.platform.core.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
