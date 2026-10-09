package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * inventory.deducted — DB 차감 확정(커밋 후). 추가 필드 없음.
 */
public record InventoryDeductedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
