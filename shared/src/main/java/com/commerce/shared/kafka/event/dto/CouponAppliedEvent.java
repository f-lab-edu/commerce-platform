package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CouponAppliedEvent(
    String orderId, long discountAmt, long originAmt,
    List<ItemEntry> items, String customerId, String couponId,
    String payMethod, String payProvider,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
