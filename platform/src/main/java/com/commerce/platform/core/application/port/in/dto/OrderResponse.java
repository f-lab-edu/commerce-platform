package com.commerce.platform.core.application.port.in.dto;


import com.commerce.platform.core.domain.aggreate.Order;

import java.time.format.DateTimeFormatter;

public record OrderResponse (
        String orderId,
        String status,
        String orderedDateTime,
        String updatedDateTime
) {
    /**
     * 주문 생성 응답
     * domain -> api dto
     */
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId().id(),
                order.getStatus().getValue(),
                order.getOrderedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd- HH:mm")),
                null
        );
    }

    /**
     * 주문 취소 응답
     */
    public static OrderResponse ofCanceled(Order order) {
        return new OrderResponse(
                order.getOrderId().id(),
                order.getStatus().getValue(),
                order.getOrderedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd- HH:mm")),
                order.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd- HH:mm"))
        );
    }
}
