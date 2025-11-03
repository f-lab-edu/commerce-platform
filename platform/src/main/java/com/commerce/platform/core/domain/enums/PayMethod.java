package com.commerce.platform.core.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayMethod {
    CARD("카드결제"),
    PHONE("휴대폰결제"),
    JE_PAY("등록된 카드로 결제");

    private final String value;
}
