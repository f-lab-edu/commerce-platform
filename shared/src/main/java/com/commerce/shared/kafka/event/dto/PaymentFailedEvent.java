package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * payment.failed — 결제 실패. payments-DB가 실패 거래(payment_failure) 저장 후 발행한다. 구독: order, inventory(Redis/DB 복원), coupon.
 */
public record PaymentFailedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    long originAmt, long discountAmt,
    String reason,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
