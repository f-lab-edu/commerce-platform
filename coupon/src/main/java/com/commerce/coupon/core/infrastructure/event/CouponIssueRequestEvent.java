package com.commerce.coupon.core.infrastructure.event;

import com.commerce.shared.kafka.event.dto.DomainEvent;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;

import java.time.LocalDateTime;

/**
 * 쿠폰 발급 요청 이벤트
 */
public record CouponIssueRequestEvent(
    String couponId,
    String customerId,
    String key,
    LocalDateTime timestamp
) implements DomainEvent {

    public static CouponIssueRequestEvent of(CouponId couponId, CustomerId customerId) {
        return new CouponIssueRequestEvent(
                couponId.id(),
                customerId.id(),
                new StringBuilder(couponId.id()).append(":")
                        .append(customerId.id()).toString(),
                LocalDateTime.now());
    }
}
