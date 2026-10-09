package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * inventory.deduct-failed — Redis 예약 실패 또는 DB 차감 실패. 구독: order.
 */
public record InventoryDeductFailedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    String reason,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
