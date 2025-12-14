package com.commerce.payments.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionStatus {
    PENDING("결제 대기"),
    FAILED("결제 실패"),
    APPROVAL("승인"),
    CANCELED("취소"),
    PART_CANCELED("부분취소");

    private final String value;
}
