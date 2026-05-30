package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SagaTimeoutEvent(
    String orderId, String customerId, String couponId, List<ItemEntry> items,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
