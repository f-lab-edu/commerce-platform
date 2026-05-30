package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;

public record InventoryDeductFailedEvent(
    String orderId, String reason, String key, LocalDateTime timestamp
) implements DomainEvent { }
