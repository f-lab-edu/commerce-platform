package com.commerce.platform.core.domain.vo;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serializable;

@Embeddable
public record CouponIssueId(
        @Embedded
        @AttributeOverride(name = "id", column = @Column(name = "coupon_id", nullable = false, length = 21))
        CouponId couponId,

        @Embedded
        @AttributeOverride(name = "id", column = @Column(name = "customer_id", nullable = false, length = 21))
        CustomerId customerId
) implements Serializable {
}
