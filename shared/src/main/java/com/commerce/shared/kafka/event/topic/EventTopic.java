package com.commerce.shared.kafka.event.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 이벤트 topic 정의
 */
@Getter
@AllArgsConstructor
public enum EventTopic {
    ORDER_COMPLETED_TOPIC("order.completed"),
    COUPON_ISSUE_TOPIC("coupon-issue-request"),
    ORDER_CREATED_TOPIC("order.created"),
    INVENTORY_DEDUCTED_TOPIC("inventory.deducted"),
    INVENTORY_DEDUCT_FAILED_TOPIC("inventory.deduct-failed"),
    ORDER_PRICED_TOPIC("order.priced"),
    ORDER_PRICE_FAILED_TOPIC("order.price-failed"),
    COUPON_APPLIED_TOPIC("coupon.applied"),
    COUPON_APPLY_FAILED_TOPIC("coupon.apply-failed"),
    PAYMENT_COMPLETED_TOPIC("payment.completed"),
    PAYMENT_FAILED_TOPIC("payment.failed"),
    SAGA_TIMEOUT_TOPIC("saga.timeout")
    ;

    private final String value;
}
