package com.commerce.payments.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    APPROVED("approved"),
    FULL_CANCELED("fullCanceled"),
    PARTIAL_CANCELED("partialCanceled"),
    FAILED("failed");

    private final String value;
}
