package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * inventory.reserved (inventory 내부) — Redis 예약 확정. 추가 필드 없음. key = orderId.
 */
public record InventoryReservedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
