package com.commerce.coupon.bootstrap.dto;

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
