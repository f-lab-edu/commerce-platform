package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * payment.canceled — 결제취소(환불) 성공 후 payments-DB가 발행한다.
 */
public record PaymentCanceledEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    String paymentId, String pgCancelTid, long cancelAmt, LocalDateTime canceledAt,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
