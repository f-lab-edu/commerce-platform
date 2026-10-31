package com.commerce.inventory.bootstrap.dto;

import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Inventory 모듈 관점의 재고 복구 트리거 페이로드.
 *
 * order.price-failed / coupon.apply-failed / payment.failed 보상 이벤트 소비.
 * producer 이벤트의 items(List&lt;ItemEntry&gt;)를 그대로 투영한다. 필드명이 다르면 역직렬화 시 null이 되므로 주의.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryRestoreEvent(
        String orderId,
        List<ItemEntry> items
) { }
