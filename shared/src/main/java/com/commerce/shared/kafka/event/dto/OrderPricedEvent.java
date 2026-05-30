package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderPricedEvent(
    String orderId, String customerId, String couponId,
    long originAmt, List<ItemEntry> items, String payMethod, String payProvider,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
