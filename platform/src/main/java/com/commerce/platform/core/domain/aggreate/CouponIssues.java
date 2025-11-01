package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.enums.CouponIssueStatus;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.CouponIssueId;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.shared.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.commerce.platform.core.domain.enums.CouponIssueStatus.EXPIRED;
import static com.commerce.platform.core.domain.enums.CouponIssueStatus.USED;
import static com.commerce.platform.shared.exception.BusinessError.EXPIRED_ISSUED_COUPON;
import static com.commerce.platform.shared.exception.BusinessError.USED_ISSUED_COUPON;

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

    @Column(name = "issued_at", nullable = false)
    LocalDateTime issuedAt;

    @Column(name = "usedAt", nullable = false)
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
        this.status = USED;
        this.usedAt = LocalDateTime.now();
        this.orderId = orderId;
    }

    public void valid() {
        if(this.status == USED) throw new BusinessException(USED_ISSUED_COUPON);
        else if(this.status == EXPIRED) throw new BusinessException(EXPIRED_ISSUED_COUPON);
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
