package com.commerce.platform.infrastructure.adaptor;

import com.commerce.platform.core.application.out.CouponIssueOutPort;
import com.commerce.platform.core.domain.aggreate.CouponIssues;
import com.commerce.platform.core.domain.vo.CouponIssueId;
import com.commerce.shared.vo.CustomerId;
import com.commerce.platform.infrastructure.persistence.CouponIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CouponIssueAdaptor implements CouponIssueOutPort{
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
