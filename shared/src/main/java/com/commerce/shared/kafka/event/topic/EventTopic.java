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
    COUPON_ISSUE_TOPIC("coupon-issue-request")
    ;

    private final String value;
}
