package com.commerce.coupon.core.domain.aggregate;

import com.commerce.coupon.core.domain.enums.CouponIssueStatus;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CouponIssueId;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.OrderId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


import static com.commerce.shared.exception.BusinessError.EXPIRED_ISSUED_COUPON;
import static com.commerce.shared.exception.BusinessError.USED_ISSUED_COUPON;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupon_issue")
@Entity
public class CouponIssues {
    @EmbeddedId
    private CouponIssueId couponIssueId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    CouponIssueStatus status;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "order_id", length = 21))
    OrderId orderId;

    @Column(name = "issued_at", nullable = false, updatable = false)
    LocalDateTime issuedAt;

    @Column(name = "usedAt")
    LocalDateTime usedAt;

    public static CouponIssues create(
            CouponId couponId,
            CustomerId customerId
    ) {
        return CouponIssues.builder()
                .couponIssueId(new CouponIssueId(couponId, customerId))
                .status(CouponIssueStatus.UNUSED)
                .issuedAt(LocalDateTime.now())
                .build();
    }
    /**
     * 쿠폰 사용처리
     */
    public void use(OrderId orderId) {
        this.status = CouponIssueStatus.USED;
        this.usedAt = LocalDateTime.now();
        this.orderId = orderId;
    }

    public void valid() {
        if(this.status == CouponIssueStatus.USED) throw new BusinessException(USED_ISSUED_COUPON);
        else if(this.status == CouponIssueStatus.EXPIRED) throw new BusinessException(EXPIRED_ISSUED_COUPON);
    }

    /**
     * 쿠폰 복구 (보상 트랜잭션) — 주문 단위로 스코프.
     *
     * - USED이고 소유 주문이 일치 → UNUSED로 복구.
     * - USED이지만 다른 주문 소유 → 무시(타 주문의 사용 상태 보호).
     */
    public void restore(OrderId orderId) {
        if (this.status != CouponIssueStatus.USED) return;
        if (!orderId.equals(this.orderId)) return;

        this.status = CouponIssueStatus.UNUSED;
        this.orderId = null;
        this.usedAt = null;
    }

    @Builder
    private CouponIssues(CouponIssueId couponIssueId, CouponIssueStatus status,
                         OrderId orderId, LocalDateTime issuedAt, LocalDateTime usedAt) {
        this.couponIssueId = couponIssueId;
        this.status = status;
        this.orderId = orderId;
        this.issuedAt = issuedAt;
        this.usedAt = usedAt;
    }
}
