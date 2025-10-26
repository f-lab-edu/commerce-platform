package com.commerce.platform.bootstrap.dto.order;

import com.commerce.platform.core.domain.aggreate.Order;

public record OrderRefundResponse(
        String orderId,
        long refundAmount,
        String status
) {
    public static OrderRefundResponse from(Order refundedOrder) {
        return new OrderRefundResponse(
                refundedOrder.getOrderId().id(),
                refundedOrder.getResultAmt().value(),
                refundedOrder.getStatus().getValue()
        );
    }
}
