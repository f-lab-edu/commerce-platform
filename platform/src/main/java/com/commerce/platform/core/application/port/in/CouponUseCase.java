package com.commerce.platform.core.application.port.in;

import com.commerce.platform.bootstrap.dto.coupon.CouponRequest;
import com.commerce.platform.core.domain.aggreate.Coupon;

public interface CouponUseCase {
    Coupon createCoupon(CouponRequest couponRequest);
}
