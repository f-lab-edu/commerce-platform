package com.commerce.shared.kafka.event.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 이벤트 topic 정의
 */
@Getter
@AllArgsConstructor
public enum EventTopic {
    ORDER_COMPLETED_TOPIC("order.completed");

    private final String value;
}
