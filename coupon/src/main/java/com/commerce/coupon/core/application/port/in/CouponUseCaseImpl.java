package com.commerce.coupon.core.application.port.in;


import com.commerce.coupon.bootstrap.dto.CouponRequest;
import com.commerce.coupon.core.application.port.out.CouponOutPort;
import com.commerce.coupon.core.domain.aggregate.Coupon;
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
