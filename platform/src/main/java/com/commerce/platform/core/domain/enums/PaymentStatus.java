package com.commerce.platform.core.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    APPROVED("approved"),
    FULL_CANCELED("fullCanceled"),
    PARTIAL_CANCELED("partialCanceled");

    private final String value;
}
