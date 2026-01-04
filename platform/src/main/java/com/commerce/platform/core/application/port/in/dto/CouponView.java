package com.commerce.platform.core.application.port.in.dto;

public record CouponView(
        String couponName,
        int discountPercent,
        long minOrderAmt,
        long maxDiscountAmt,
        String status, // 사용, 미사용, 만료
        int daysUntilExpiration
) {
}
