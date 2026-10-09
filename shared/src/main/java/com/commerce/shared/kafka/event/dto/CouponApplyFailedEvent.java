package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * coupon.apply-failed — 쿠폰 적용 실패. 구독: order, inventory(Redis/DB 복원).
 */
public record CouponApplyFailedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    long originAmt,
    String reason,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
