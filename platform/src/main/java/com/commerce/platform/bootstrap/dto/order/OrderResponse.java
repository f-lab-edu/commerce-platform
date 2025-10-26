package com.commerce.platform.bootstrap.dto.order;


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
                order.getOrderedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd- HH:mm")),
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
                null,
                order.getUpdatedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd- HH:mm"))
        );
    }
}
