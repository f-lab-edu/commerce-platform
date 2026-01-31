package com.commerce.coupon.core.application.port.in;

import com.commerce.coupon.core.application.port.in.dto.CouponView;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;

import java.util.List;

public interface CouponIssueUseCase {
    List<CouponView> getMyCoupons(CustomerId customerId);
    void issueCoupon(CouponId couponId, CustomerId customerId);
}
