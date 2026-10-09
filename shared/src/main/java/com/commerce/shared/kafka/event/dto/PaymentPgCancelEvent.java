package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * payment.pg-cancel (payments 내부) — 망취소(VOID) 또는 결제취소(REFUND) 요청.
 * 취소 유형은 Kafka 헤더 {@link com.commerce.shared.kafka.event.type.EventHeaders#CANCEL_TYPE}로 전달한다.
 * pgApprovedAmt / pgTid / cancelAmt 는 상황에 따라 null.
 */
public record PaymentPgCancelEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    long originAmt, long discountAmt,
    String reason, long requestedAmt, Long pgApprovedAmt, String pgTid, Long cancelAmt,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
