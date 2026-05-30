package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

public record InventoryDeductedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
