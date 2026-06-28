package com.commerce.coupon.bootstrap.event.dto;

import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Coupon 모듈 관점의 쿠폰 적용 트리거 페이로드.
 *
 * order.priced 토픽 소비. 쿠폰 적용 로직 + 후속 CouponAppliedEvent 발행에 필요한 필드만 보유.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CouponApplyEvent(
        String orderId,
        String customerId,
        String couponId,
        long originAmt,
        List<ItemEntry> items,
        String payMethod,
        String payProvider
) { }
