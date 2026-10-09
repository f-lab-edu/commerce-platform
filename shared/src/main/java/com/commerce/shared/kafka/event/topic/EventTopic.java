package com.commerce.shared.kafka.event.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 이벤트 topic 정의
 */
@Getter
@AllArgsConstructor
public enum EventTopic {
    COUPON_ISSUE_TOPIC("coupon-issue-request"),

    // 주문
    ORDER_CREATED_TOPIC("order.created"),
    ORDER_CONFIRMED_TOPIC("order.confirmed"),

    // 재고 (Redis 예약 → DB 차감)
    INVENTORY_RESERVED_TOPIC("inventory.reserved"),
    INVENTORY_RESERVE_ROLLBACK_TOPIC("inventory.reserve-rollback"),
    INVENTORY_DEDUCTED_TOPIC("inventory.deducted"),
    INVENTORY_DEDUCT_FAILED_TOPIC("inventory.deduct-failed"),

    // 금액 계산
    ORDER_PRICED_TOPIC("order.priced"),
    ORDER_PRICE_FAILED_TOPIC("order.price-failed"),

    // 쿠폰
    COUPON_APPLIED_TOPIC("coupon.applied"),
    COUPON_APPLY_FAILED_TOPIC("coupon.apply-failed"),

    // 결제 (pg-cancel / pg-result 는 payments 내부)
    PAYMENT_PG_CANCEL_TOPIC("payment.pg-cancel"),
    PAYMENT_PG_RESULT_TOPIC("payment.pg-result"),
    PAYMENT_COMPLETED_TOPIC("payment.completed"),
    PAYMENT_FAILED_TOPIC("payment.failed"),
    PAYMENT_CANCELED_TOPIC("payment.canceled")
    ;

    private final String value;
}
