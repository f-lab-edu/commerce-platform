package com.commerce.platform.bootstrap.dto.order;


import com.commerce.platform.core.domain.aggreate.Order;

public record OrderResponse (
        String orderId,
        String status
) {
    /**
     * domain -> api dto
     */
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getStatus().getValue()
        );
    }
}
