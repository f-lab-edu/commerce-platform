package com.commerce.coupon.core.domain.aggregate;

import com.commerce.coupon.core.domain.enums.CouponIssueStatus;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CouponIssuesTest {

    @DisplayName("restore()는 USED 상태를 UNUSED로 복구한다")
    @Test
    void restoreFromUsed() {
        CouponIssues issue = CouponIssues.create(CouponId.of("C001"), CustomerId.of("CU001"));
        issue.use(OrderId.of("O001"));
        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.USED);

        issue.restore();

        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.UNUSED);
        assertThat(issue.getOrderId()).isNull();
        assertThat(issue.getUsedAt()).isNull();
    }

    @DisplayName("restore()는 UNUSED 상태에서 아무것도 하지 않는다 (멱등)")
    @Test
    void restoreFromUnusedIsIdempotent() {
        CouponIssues issue = CouponIssues.create(CouponId.of("C001"), CustomerId.of("CU001"));
        issue.restore();
        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.UNUSED);
    }
}
