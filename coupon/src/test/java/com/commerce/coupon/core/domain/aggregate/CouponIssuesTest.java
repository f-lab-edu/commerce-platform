package com.commerce.coupon.core.domain.aggregate;

import com.commerce.coupon.core.domain.enums.CouponIssueStatus;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CouponIssuesTest {

    private CouponIssues newIssue() {
        return CouponIssues.create(CouponId.of("C001"), CustomerId.of("CU001"));
    }

    @DisplayName("restore(orderId)는 소유 주문이 USED 상태를 UNUSED로 복구한다")
    @Test
    void restoreFromUsedBySameOrder() {
        CouponIssues issue = newIssue();
        OrderId o1 = OrderId.of("O001");
        issue.use(o1);
        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.USED);

        issue.restore(o1);

        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.UNUSED);
        assertThat(issue.getUsedAt()).isNull();
        assertThat(issue.getOrderId()).isNull();
    }

    @DisplayName("restore(orderId)는 다른 주문이 사용 중인 쿠폰을 건드리지 않는다")
    @Test
    void restoreIgnoresDifferentOrder() {
        CouponIssues issue = newIssue();
        OrderId o1 = OrderId.of("O001");
        OrderId o2 = OrderId.of("O002");
        issue.use(o1);

        issue.restore(o2);

        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.USED);
        assertThat(issue.getOrderId()).isEqualTo(o1);
    }

    @DisplayName("restore(orderId)는 여러 번 호출해도 멱등이다")
    @Test
    void restoreIsIdempotent() {
        CouponIssues issue = newIssue();
        OrderId o1 = OrderId.of("O001");
        issue.use(o1);

        issue.restore(o1);
        issue.restore(o1);
        issue.restore(o1);

        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.UNUSED);
        assertThat(issue.getOrderId()).isNull();
    }

    @DisplayName("미사용 쿠폰에 restore가 도착해도 상태를 변경하지 않는다")
    @Test
    void restoreOnUnusedIsNoOp() {
        CouponIssues issue = newIssue();
        OrderId o1 = OrderId.of("O001");

        issue.restore(o1);

        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.UNUSED);
        assertThat(issue.getOrderId()).isNull();
    }

    @DisplayName("보상 후 다른 주문이 쿠폰을 재사용할 수 있다")
    @Test
    void reusableByNewOrderAfterCompensation() {
        CouponIssues issue = newIssue();
        OrderId o1 = OrderId.of("O001");
        OrderId o2 = OrderId.of("O002");
        issue.use(o1);
        issue.restore(o1);

        issue.use(o2);

        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.USED);
        assertThat(issue.getOrderId()).isEqualTo(o2);
    }
}
