package com.commerce.payments.infrastructure.pg.toss;

public record TossCancelResponse(
        String transactionKey,
        String cancelStatus,
        Long cancelAmount,
        String cancelReason
) {
}
