package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;

public record PaymentCompletedEvent(
    String orderId, long originAmt, long discountAmt,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
