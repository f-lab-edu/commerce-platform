package com.commerce.coupon.core.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CouponIssueStatus {
    UNUSED("unused"),
    USED("used"),
    EXPIRED("expired")
    ;

    private final String value;
}
