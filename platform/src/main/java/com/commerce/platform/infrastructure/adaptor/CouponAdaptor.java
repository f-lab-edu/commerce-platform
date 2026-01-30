package com.commerce.platform.infrastructure.adaptor;

import com.commerce.platform.core.application.port.out.CouponOutPort;
import com.commerce.platform.core.domain.aggreate.Coupon;
import com.commerce.shared.vo.CouponId;
import com.commerce.platform.infrastructure.persistence.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CouponAdaptor implements CouponOutPort {
    private final CouponRepository repository;

    @Override
    public Coupon save(Coupon coupon) {
        return repository.save(coupon);
    }

    @Override
    public Optional<Coupon> findById(CouponId couponId) {
        return repository.findById(couponId);
    }

    @Override
    public List<Coupon> findByIdIn(List<CouponId> couponIds) {
        return repository.findAllById(couponIds);
    }
}
