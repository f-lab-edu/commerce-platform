package com.commerce.united.port.in.dto;

public record OrderRefundRequest (
    String reason,
    String bankCode,
    String accountNumber,
    String accountHolder
) {
}
