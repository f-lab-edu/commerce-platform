package com.commerce.united.port.in.dto;

import com.commerce.united.domain.aggregate.Order;

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
