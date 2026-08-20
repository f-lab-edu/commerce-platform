package com.commerce.coupon.core.application.port.in;

import com.commerce.coupon.core.application.port.in.dto.CouponView;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;

import java.util.List;

public interface CouponIssueUseCase {
    List<CouponView> getMyCoupons(CustomerId customerId);
    void issueCoupon(CouponId couponId, CustomerId customerId);
    void requestIssueCoupon(CouponId couponId, CustomerId customerId);
    boolean checkCouponIssueStatus(CouponId couponId, CustomerId customerId);
    long applyCouponForSaga(String couponId, String customerId, String orderId, long originAmt);
    void restoreCouponForSaga(String couponId, String customerId, String orderId);
}
