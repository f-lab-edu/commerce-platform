package com.commerce.coupon.bootstrap.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Coupon 모듈 관점의 쿠폰 복구 트리거 페이로드.
 *
 * payment.failed / saga.timeout 보상 이벤트 소비. 쿠폰 복구에 필요한 식별자만 보유.
 * couponId/customerId는 null 가능 (쿠폰 미사용 주문).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CouponRestoreEvent(
        String orderId,
        String couponId,
        String customerId
) { }
