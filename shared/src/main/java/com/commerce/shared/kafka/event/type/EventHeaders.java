package com.commerce.shared.kafka.event.type;

/**
 * 이벤트 Kafka 헤더 이름 정의
 */
public final class EventHeaders {
    /** payment.pg-cancel 취소 유형 ({@link CancelType}) */
    public static final String CANCEL_TYPE = "cancelType";

    private EventHeaders() {
    }
}
