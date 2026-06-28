package com.commerce.inventory.bootstrap.dto;

import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Inventory 모듈 관점의 재고 차감 트리거 페이로드.
 *
 * order.created 토픽 소비. 재고 차감 + 후속 InventoryDeductedEvent 발행에 필요한 필드 보유.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryDeductEvent(
        String orderId,
        String customerId,
        String couponId,
        List<ItemEntry> items,
        String payMethod,
        String payProvider
) { }
