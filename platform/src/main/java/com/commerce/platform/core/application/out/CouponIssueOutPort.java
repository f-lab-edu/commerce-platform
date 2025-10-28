package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.CouponIssue;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.CustomerId;

import java.util.Optional;

public interface CouponIssueOutPort {
    Optional<CouponIssue> findByIdCustomerId(CouponId couponId, CustomerId customerId);
}
