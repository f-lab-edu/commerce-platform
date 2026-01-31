package com.commerce.coupon.core.application.port.out;

import com.commerce.coupon.core.domain.aggregate.Coupon;
import com.commerce.shared.vo.CouponId;

import java.util.List;
import java.util.Optional;

public interface CouponOutPort {
    Coupon save(Coupon coupon);
    Optional<Coupon> findById(CouponId couponId);
    List<Coupon> findByIdIn(List<CouponId> couponIds);
}
