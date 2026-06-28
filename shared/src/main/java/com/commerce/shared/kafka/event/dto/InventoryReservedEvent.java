package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Redis 예약(차감) 확정 신호. DB 차감 컨슈머(B)가 소비한다. key = orderId. */
public record InventoryReservedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
