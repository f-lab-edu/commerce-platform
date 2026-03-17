package com.commerce.shared.kafka.event.dto;

import com.commerce.shared.vo.OrderId;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 주문 완료 이벤트 (order.completed)
 * 주문이 검증되고 결제 준비가 완료되면 발행됨
 */
public record OrderCompletedEvent (
    OrderId orderId,
    List<ProductId> productIds,
    Map<ProductId, Quantity> quantities,
    LocalDateTime timestamp,
    String key
) implements DomainEvent {
    public static OrderCompletedEvent of(
            OrderId orderId,
            List<ProductId> productIds,
            Map<ProductId, Quantity> quantities
    ) {
        return new OrderCompletedEvent(
                orderId,
                productIds,
                quantities,
                LocalDateTime.now(),
                "key"
        );
    }
}
