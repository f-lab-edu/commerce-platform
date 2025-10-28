package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.enums.CouponIssueStatus;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.shared.exception.BusinessException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static com.commerce.platform.core.domain.enums.CouponIssueStatus.EXPIRED;
import static com.commerce.platform.core.domain.enums.CouponIssueStatus.USED;
import static com.commerce.platform.shared.exception.BusinessError.EXPIRED_ISSUED_COUPON;
import static com.commerce.platform.shared.exception.BusinessError.USED_ISSUED_COUPON;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class CouponIssue {
    CouponId couponId;
    CustomerId customerId;
    CouponIssueStatus status;
    OrderId orderId;
    LocalDateTime issuedAt;
    LocalDateTime usedAt;

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
}
