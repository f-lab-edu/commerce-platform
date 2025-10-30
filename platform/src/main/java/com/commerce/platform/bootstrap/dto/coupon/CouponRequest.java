package com.commerce.platform.bootstrap.dto.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record CouponRequest(
        String couponName,
        String code,
        int discountPercent,
        long minOrderAmt,
        long maxDiscountAmt,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate frDt,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate toDt,
        long quantity
) {
}
