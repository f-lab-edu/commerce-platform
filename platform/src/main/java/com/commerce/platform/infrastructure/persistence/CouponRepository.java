package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.Coupon;
import com.commerce.platform.core.domain.vo.CouponId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, CouponId> {
}
