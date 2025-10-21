package com.commerce.united.controller.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController("/coupon")
public class CouponController {

    @GetMapping("/{couponId}")
    public String downloadCoupon(@PathVariable String couponId) {
        return null;
    }
}
