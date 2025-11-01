package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.CouponIssues;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.CustomerId;

import java.util.List;
import java.util.Optional;

public interface CouponIssueOutPort {
    Optional<CouponIssues> findByIdCustomerId(CouponId couponId, CustomerId customerId);
    List<CouponIssues> findByCustomerId(CustomerId customerId);
    void save(CouponIssues issuedCoupon);
}
