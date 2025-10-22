package com.commerce.united.port.in.dto;

public record OrderRefundResponse(
        String orderId,
        long refundAmount,
        String status
) {
}
