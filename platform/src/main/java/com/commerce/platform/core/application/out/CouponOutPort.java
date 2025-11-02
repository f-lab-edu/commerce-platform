package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.Coupon;
import com.commerce.platform.core.domain.vo.CouponId;

import java.util.List;
import java.util.Optional;

public interface CouponOutPort {
    Coupon save(Coupon coupon);
    Optional<Coupon> findById(CouponId couponId);
    List<Coupon> findByIdIn(List<CouponId> couponIds);
}
