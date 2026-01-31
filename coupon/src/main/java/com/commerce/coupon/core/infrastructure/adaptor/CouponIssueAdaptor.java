package com.commerce.coupon.core.infrastructure.adaptor;

import com.commerce.coupon.core.application.port.out.CouponIssueOutPort;
import com.commerce.coupon.core.domain.aggregate.CouponIssues;
import com.commerce.coupon.core.infrastructure.persistence.CouponIssueRepository;
import com.commerce.shared.vo.CouponIssueId;
import com.commerce.shared.vo.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CouponIssueAdaptor implements CouponIssueOutPort {
    private final CouponIssueRepository repository;

    @Override
    public Optional<CouponIssues> findByCouponIssueId(CouponIssueId couponIssueId) {
        return repository.findById(couponIssueId);
    }

    @Override
    public List<CouponIssues> findByCustomerId(CustomerId customerId) {
        return repository.findAllByCustomerId(customerId);
    }

    @Override
    public CouponIssues save(CouponIssues issuedCoupon) {
        return repository.save(issuedCoupon);
    }
}
