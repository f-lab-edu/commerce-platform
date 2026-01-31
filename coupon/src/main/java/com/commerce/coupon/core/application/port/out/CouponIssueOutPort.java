package com.commerce.coupon.core.application.port.out;

import com.commerce.coupon.core.domain.aggregate.CouponIssues;
import com.commerce.shared.vo.CouponIssueId;
import com.commerce.shared.vo.CustomerId;

import java.util.List;
import java.util.Optional;

public interface CouponIssueOutPort {
    Optional<CouponIssues> findByCouponIssueId(CouponIssueId couponIssueId);
    List<CouponIssues> findByCustomerId(CustomerId customerId);
    CouponIssues save(CouponIssues issuedCoupon);
}
