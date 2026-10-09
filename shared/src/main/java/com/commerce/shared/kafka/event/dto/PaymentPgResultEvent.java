package com.commerce.shared.kafka.event.dto;

import com.commerce.shared.kafka.event.type.PgResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * payment.pg-result (payments 내부) — payments-PG가 PG 결과를 발행하고 payments-DB가 저장한다.
 * APPROVED / CANCELED 는 payment, 나머지는 payment_failure 에 저장한다.
 */
public record PaymentPgResultEvent(
    String orderId, String customerId, String couponId,
    List<ItemEntry> items, String payMethod, String payProvider, int installment,
    long originAmt, long discountAmt,
    PgResult result, String reason, long requestedAmt, Long pgApprovedAmt,
    String pgProvider, String pgResponseCode, String pgTid, String pgCancelTid,
    LocalDateTime approvedAt, LocalDateTime canceledAt,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
