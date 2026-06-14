package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Redis 게이트가 실제로 복원(팬텀 가드 통과)한 주문의 복원 결과.
 * 비동기 durable 원장(InventoryLedgerConsumer)이 이를 소비해 DB 재고를 따라 복원한다.
 */
public record InventoryRestoredEvent(
    String orderId, List<ItemEntry> items,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
