package com.commerce.inventory.bootstrap.dto;

import com.commerce.shared.kafka.event.dto.DomainEvent;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Inventory 모듈 관점의 재고 복구 트리거 페이로드.
 *
 * order.price-failed / coupon.apply-failed / payment.failed / saga.timeout 보상 이벤트 소비.
 * items가 누락된 producer 이벤트도 있을 수 있어 null 가능.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryRestoreEvent(
        String orderId,
        ItemEntry item
) implements DomainEvent {
    @Override public String key() { return orderId; }
    @Override public LocalDateTime timestamp() { return null; }
}
