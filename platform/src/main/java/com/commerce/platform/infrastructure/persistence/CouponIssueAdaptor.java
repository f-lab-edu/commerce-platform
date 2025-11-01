package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.application.out.CouponIssueOutPort;
import com.commerce.platform.core.domain.aggreate.CouponIssues;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.CustomerId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CouponIssueAdaptor implements CouponIssueOutPort {
    @Override
    public Optional<CouponIssues> findByIdCustomerId(CouponId couponId, CustomerId customerId) {
        return null;
    }

    @Override
    public List<CouponIssues> findByCustomerId(CustomerId customerId) {
        return null;
    }

    @Override
    public void save(CouponIssues issuedCoupon) {

    }
}
