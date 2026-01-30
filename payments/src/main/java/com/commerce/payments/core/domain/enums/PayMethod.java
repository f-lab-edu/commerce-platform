package com.commerce.payments.core.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayMethod {
    CARD("카드결제"),
    EASY_PAY("간편결제"),
    PHONE("휴대폰결제"),
    VIRTUAL_ACCOUNT("가상계좌결제");

    private final String value;
}
