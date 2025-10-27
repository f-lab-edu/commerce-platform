package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.Coupon;
import com.commerce.platform.core.domain.vo.CouponId;

public interface CouponOutPort {
    Coupon findById(CouponId couponId);
}
