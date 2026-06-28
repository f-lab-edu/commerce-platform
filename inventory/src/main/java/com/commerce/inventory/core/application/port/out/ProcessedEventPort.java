package com.commerce.inventory.core.application.port.out;

/**
 * 이벤트 멱등 처리 아웃바운드 포트. eventId 단위로 1회 처리를 보장한다.
 * 비동기 원장 컨슈머가 Kafka redelivery 시 DB 재고를 이중 반영하지 않도록 check-then-process에 사용.
 */
public interface ProcessedEventPort {

    /** 이미 처리된 eventId인가. */
    boolean exists(String eventId);

    /** eventId를 처리 완료로 기록한다. */
    void markProcessed(String eventId);
}
