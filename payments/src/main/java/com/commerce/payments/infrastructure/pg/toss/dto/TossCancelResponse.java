package com.commerce.payments.infrastructure.pg.toss.dto;

public record TossCancelResponse(
        String transactionKey,
        String cancelStatus,
        Long cancelAmount,
        String cancelReason
) {
}
