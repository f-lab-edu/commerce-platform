package com.commerce.coupon.bootstrap;

import com.commerce.coupon.bootstrap.dto.CouponRequest;
import com.commerce.coupon.core.application.port.in.CouponUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/admin/coupons")
@RestController
public class CouponAdController {
    private final CouponUseCase couponUseCase;

    @PostMapping
    public ResponseEntity<String> createCoupon(@RequestBody CouponRequest couponRequest) {
        couponUseCase.createCoupon(couponRequest);
        return ResponseEntity.ok("성공");
    }
}
