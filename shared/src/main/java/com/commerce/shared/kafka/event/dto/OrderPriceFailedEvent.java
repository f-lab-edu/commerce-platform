package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * order.price-failed — 금액 계산 실패. 구독: order, inventory(Redis/DB 복원).
 */
public record OrderPriceFailedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    String reason,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
