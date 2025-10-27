package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.application.out.CouponOutPort;
import com.commerce.platform.core.domain.aggreate.Coupon;
import com.commerce.platform.core.domain.vo.CouponId;
import org.springframework.stereotype.Repository;

@Repository
public class CouponAdaptor implements CouponOutPort {
    @Override
    public Coupon findById(CouponId couponId) {
        return null;
    }
}
