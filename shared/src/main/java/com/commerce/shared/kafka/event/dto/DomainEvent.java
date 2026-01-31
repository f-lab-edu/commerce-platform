package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;

/**
 * 이벤트에 필요한 요소 정의
 */
public interface DomainEvent {
    String key();
    LocalDateTime timestamp();
}
