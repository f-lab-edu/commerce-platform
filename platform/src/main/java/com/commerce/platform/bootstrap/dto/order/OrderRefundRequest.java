package com.commerce.platform.bootstrap.dto.order;

public record OrderRefundRequest (
    String reason,
    String bankCode,
    String accountNumber,
    String accountHolder
) {
}
