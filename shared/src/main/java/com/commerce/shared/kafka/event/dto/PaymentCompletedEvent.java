package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * payment.completed — payments-DB가 정상 승인 거래 저장 후 발행한다.
 */
public record PaymentCompletedEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    long originAmt, long discountAmt,
    String paymentId, long approvedAmt, String pgProvider, LocalDateTime approvedAt,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
