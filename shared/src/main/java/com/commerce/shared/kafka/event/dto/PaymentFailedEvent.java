package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PaymentFailedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String reason,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
