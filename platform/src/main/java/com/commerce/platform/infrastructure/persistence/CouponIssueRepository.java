package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.domain.aggreate.CouponIssues;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.CouponIssueId;
import com.commerce.platform.core.domain.vo.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponIssueRepository extends JpaRepository<CouponIssues, CouponIssueId> {

    @Query("SELECT COUNT(ci) FROM CouponIssues ci WHERE ci.couponIssueId.couponId = :couponId")
    Long countByCouponId(CouponId couponId);

    @Query("SELECT ci FROM CouponIssues ci WHERE ci.couponIssueId.customerId = :customerId")
    List<CouponIssues> findAllByCustomerId(CustomerId customerId);
}
