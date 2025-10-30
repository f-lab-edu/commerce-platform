package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.application.out.CouponOutPort;
import com.commerce.platform.core.domain.aggreate.Coupon;
import com.commerce.platform.core.domain.vo.CouponId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CouponAdaptor implements CouponOutPort {
    @Override
    public Optional<Coupon> findById(CouponId couponId) {
        return null;
    }

    @Override
    public List<Coupon> findByIdIn(List<CouponId> couponIds) {
        return null;
    }
}
