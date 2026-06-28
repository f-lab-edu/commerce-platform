package com.commerce.product.bootstrap.event.dto;

import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Product 모듈 관점의 금액 계산 트리거 페이로드.
 *
 * inventory.deducted 토픽 소비. 금액 계산 + 후속 OrderPricedEvent 발행에 필요한 필드 보유.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductPricingEvent(
        String orderId,
        String customerId,
        String couponId,
        List<ItemEntry> items,
        String payMethod,
        String payProvider
) { }
