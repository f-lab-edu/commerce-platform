package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.Coupon;
import com.commerce.shared.vo.CouponId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, CouponId> {
}
