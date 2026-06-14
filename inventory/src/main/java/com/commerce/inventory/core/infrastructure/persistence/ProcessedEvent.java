package com.commerce.inventory.core.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka 이벤트 멱등 처리 기록. eventId(=의미 키) 단위로 1회 처리를 보장한다.
 * ProcessedEventPort 어댑터가 redelivery 시 DB 재고 이중 반영을 막는 check-then-process에 사용.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "processed_events")
@Entity
public class ProcessedEvent {
    @Id
    @Column(name = "event_id", length = 100)
    private String eventId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public ProcessedEvent(String eventId) {
        this.eventId = eventId;
        this.processedAt = LocalDateTime.now();
    }
}
