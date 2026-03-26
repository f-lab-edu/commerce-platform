package com.commerce.coupon.bootstrap;

import com.commerce.coupon.core.application.port.in.CouponIssueUseCase;
import com.commerce.coupon.core.application.port.in.dto.CouponView;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/coupons")
@RestController
public class CouponController {
    private final CouponIssueUseCase couponIssueUseCase;

    @PostMapping("/{couponId}/issue/{customerId}")
    public ResponseEntity<String> issueCoupon(
            @PathVariable String couponId,
            @PathVariable String customerId
    ) {
        CouponId couId = CouponId.of(couponId);
        CustomerId cusId = CustomerId.of(customerId);

        boolean issued = couponIssueUseCase.checkCouponIssueStatus(couId, cusId);
        if(issued) {
            return ResponseEntity.ok("이미 발급된 쿠폰입니다.");
        }

        // 미발행된 경우에만 이벤트 발행
        couponIssueUseCase.requestIssueCoupon(couId, cusId);
        return ResponseEntity.ok("발급 요청 완료");
    }

    @GetMapping("/users/{customerId}")
    public ResponseEntity<List<CouponView>> getMyCoupons(@PathVariable String customerId) {
        List<CouponView> myCoupons = couponIssueUseCase.getMyCoupons(CustomerId.of(customerId));
        return ResponseEntity.ok(myCoupons);
    }

    /**
     * 쿠폰 발급 확인 API
     * Redis에서 발급 여부 확인
     */
    @GetMapping("/{couponId}/issued/{customerId}")
    public ResponseEntity<String> checkIssued(
            @PathVariable String couponId,
            @PathVariable String customerId
    ) {
        boolean isIssued = couponIssueUseCase.checkCouponIssueStatus(
                CouponId.of(couponId),
                CustomerId.of(customerId)
        );

        return ResponseEntity.ok(isIssued ? "발행완료" : "미발행");
    }
}
