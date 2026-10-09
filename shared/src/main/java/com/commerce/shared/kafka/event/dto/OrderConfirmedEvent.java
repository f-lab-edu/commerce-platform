package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * order.confirmed — order가 주문 확정 후 발행한다.
 */
public record OrderConfirmedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    long originAmt, long discountAmt, long resultAmt, String paymentId,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
