package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * order.created — order가 주문 컨텍스트(주문상품, 결제조건, 할부)를 만든다.
 */
public record OrderCreatedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
