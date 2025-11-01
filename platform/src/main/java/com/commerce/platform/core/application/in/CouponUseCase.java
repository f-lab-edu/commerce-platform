package com.commerce.platform.core.application.in;

import com.commerce.platform.bootstrap.dto.coupon.CouponRequest;

public interface CouponUseCase {
    void createCoupon(CouponRequest couponRequest);
}
