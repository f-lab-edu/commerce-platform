package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.application.out.CouponIssueOutPort;
import com.commerce.platform.core.domain.aggreate.CouponIssue;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.CustomerId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CouponIssueAdaptor implements CouponIssueOutPort {
    @Override
    public Optional<CouponIssue> findByIdCustomerId(CouponId couponId, CustomerId customerId) {
        return null;
    }
}
