package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * coupon.applied — coupon이 discountAmt를 추가한다. payments-PG의 승인 트리거.
 */
public record CouponAppliedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    long originAmt, long discountAmt,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
