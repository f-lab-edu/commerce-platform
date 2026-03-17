package com.commerce.coupon.core.infrastructure.persistence;

import com.commerce.coupon.core.domain.aggregate.Coupon;
import com.commerce.shared.vo.CouponId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, CouponId> {
}
