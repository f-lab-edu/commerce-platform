package com.commerce.platform.core.application.port.in;

import com.commerce.platform.bootstrap.dto.coupon.CouponRequest;
import com.commerce.platform.core.application.port.out.CouponOutPort;
import com.commerce.platform.core.domain.aggreate.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponUseCaseImpl implements CouponUseCase {
    private final CouponOutPort couponOutPort;

    @Override
    public Coupon createCoupon(CouponRequest couponRequest) {
        Coupon coupon = Coupon.create(
                couponRequest.code(),
                couponRequest.couponName(),
                couponRequest.discountPercent(),
                couponRequest.minOrderAmt(),
                couponRequest.maxDiscountAmt(),
                couponRequest.frDt(),
                couponRequest.toDt(),
                couponRequest.quantity()
        );

        return couponOutPort.save(coupon);
    }
}
