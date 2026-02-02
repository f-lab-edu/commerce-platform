package com.commerce.coupon.core.application.port.out;

import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;

/**
 * 쿠폰 발급 캐치
 */
public interface CouponIssueCache {
    
    /**
     * 쿠폰 발급 정보 저장
     * coupon:{couponId} {customerId}
     */
    void save(CouponId couponId, CustomerId customerId);
    
    /**
     * 쿠폰 발급 여부 확인
     * coupon:{couponId} {customerId}
     */
    boolean isIssued(CouponId couponId, CustomerId customerId);
}
