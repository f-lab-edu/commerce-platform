package com.commerce.coupon.bootstrap;

import com.commerce.coupon.core.application.port.in.CouponIssueUseCase;
import com.commerce.coupon.core.application.port.in.dto.CouponView;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/coupons")
@RestController
public class CouponController {
    private final CouponIssueUseCase couponIssueUseCase;

    @PostMapping("/{couponId}/issue/{customerId}")
    public ResponseEntity<String> issueCoupon(@PathVariable String couponId) {
        couponIssueUseCase.issueCoupon(CouponId.of(couponId), CustomerId.of("test1")); // todo tmp customerId
        return ResponseEntity.ok("발급 성공");
    }

    @GetMapping("/users/{customerId}")
    public ResponseEntity<List<CouponView>> getMyCoupons(@PathVariable String customerId) {
        List<CouponView> myCoupons = couponIssueUseCase.getMyCoupons(CustomerId.of(customerId));
        return ResponseEntity.ok(myCoupons);
    }
}
