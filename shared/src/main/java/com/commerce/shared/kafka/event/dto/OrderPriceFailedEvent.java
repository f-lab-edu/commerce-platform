package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderPriceFailedEvent(
    String orderId, List<ItemEntry> items, String reason,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
