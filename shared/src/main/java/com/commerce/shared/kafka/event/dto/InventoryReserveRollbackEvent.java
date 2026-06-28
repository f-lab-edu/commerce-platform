package com.commerce.shared.kafka.event.dto;

import java.time.LocalDateTime;
import java.util.List;

/** DB 차감이 재고 부족으로 거절될 때, A가 깐 Redis 예약을 되돌리도록 Redis 복원 컨슈머(C)에게 보내는 신호. key = orderId. */
public record InventoryReserveRollbackEvent(
    String orderId, List<ItemEntry> items,
    String key, LocalDateTime timestamp
) implements DomainEvent { }
