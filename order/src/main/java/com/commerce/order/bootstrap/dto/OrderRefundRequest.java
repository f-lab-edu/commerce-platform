package com.commerce.order.bootstrap.dto;

public record OrderRefundRequest (
    String reason,
    String bankCode,
    String accountNumber,
    String accountHolder
) {
}
