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

    /** saga 쿠폰 적용: 할인금액 계산 + 사용 처리. 쿠폰 없으면 0 반환. */
    long applyCouponForSaga(String couponId, String customerId, String orderId, long originAmt);

    /** saga 쿠폰 복구: USED -> UNUSED 복원. 멱등. */
    void restoreCouponForSaga(String couponId, String customerId);
}
