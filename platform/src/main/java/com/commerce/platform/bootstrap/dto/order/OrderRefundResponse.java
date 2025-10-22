package com.commerce.platform.bootstrap.dto.order;

public record OrderRefundResponse(
        String orderId,
        long refundAmount,
        String status
) {
}
