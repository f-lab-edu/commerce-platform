package com.commerce.platform.core.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("주문대기"),
    CONFIRMED("주문완료"),
    CANCELED("취소완료"),
    PAID("결제완료"),
    REFUND("환불완료");

    private final String value;
}
