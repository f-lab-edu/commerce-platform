package com.commerce.platform.core.application.port.out;

import com.commerce.platform.core.domain.aggreate.CouponIssues;
import com.commerce.platform.core.domain.vo.CouponIssueId;
import com.commerce.shared.vo.CustomerId;

import java.util.List;
import java.util.Optional;

public interface CouponIssueOutPort {
    Optional<CouponIssues> findByCouponIssueId(CouponIssueId couponIssueId);
    List<CouponIssues> findByCustomerId(CustomerId customerId);
    CouponIssues save(CouponIssues issuedCoupon);
}
