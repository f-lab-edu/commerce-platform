package com.commerce.platform.core.application.port.in;

import com.commerce.platform.core.application.port.in.dto.CouponView;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.shared.vo.CustomerId;

import java.util.List;

public interface CouponIssueUseCase {
    List<CouponView> getMyCoupons(CustomerId customerId);
    void issueCoupon(CouponId couponId, CustomerId customerId);
}
