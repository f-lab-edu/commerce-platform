package com.commerce.coupon.core.application.port.in;


import com.commerce.coupon.bootstrap.dto.CouponRequest;
import com.commerce.coupon.core.domain.aggregate.Coupon;

public interface CouponUseCase {
    Coupon createCoupon(CouponRequest couponRequest);
}
