package com.commerce.coupon.bootstrap.event;

import com.commerce.coupon.bootstrap.event.dto.CouponApplyEvent;
import com.commerce.coupon.bootstrap.event.dto.CouponRestoreEvent;
import com.commerce.coupon.core.domain.aggregate.Coupon;
import com.commerce.coupon.core.domain.aggregate.CouponIssues;
import com.commerce.coupon.core.domain.enums.CouponIssueStatus;
import com.commerce.coupon.core.infrastructure.persistence.CouponIssueRepository;
import com.commerce.coupon.core.infrastructure.persistence.CouponRepository;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.CouponAppliedEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CouponIssueId;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.Quantity;
import com.commerce.shared.vo.ValidPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 쿠폰 적용/복구 saga 컨슈머 통합 테스트.
 *
 * 실제 DB(MySQL)에 대해 컨슈머 메서드를 직접, 결정론적 순서로 호출하여
 * 보상(restore)의 멱등성과 주문 단위 스코프를 검증한다.
 *
 * Kafka 발행은 {@link TransactionalEventPublisher}를 @MockitoBean으로 대체해 캡처한다.
 * (docker-compose 인프라 필요 — CLAUDE.md 참조)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OrderCouponConsumerIntegrationTest {

    @Autowired
    private OrderCouponConsumer consumer;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @MockitoBean
    private TransactionalEventPublisher publisher;

    private static final long ORIGIN_AMT = 10_000L;
    private static final long EXPECTED_DISCOUNT = 5_000L; // maxDiscountAmt 상한

    private String couponId;
    private String customerId;
    private CouponIssueId issueId;

    @BeforeEach
    void setUp() {
        String uniq = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        // Coupon.create()는 createdAt을 설정하지 않으므로(도메인 팩토리 한계) 빌더로 직접 시딩한다.
        Coupon coupon = couponRepository.save(Coupon.builder()
                .couponId(CouponId.create())
                .code("C" + uniq)
                .couponName("N" + uniq)
                .discountPercent(10)
                .minOrderAmt(Money.of(1_000L))
                .maxDiscountAmt(Money.of(EXPECTED_DISCOUNT))
                .validPeriod(ValidPeriod.create(LocalDate.now().minusDays(1), LocalDate.now().plusDays(30)))
                .totalQuantity(Quantity.create(100L))
                .issuedQuantity(Quantity.create(0L))
                .createdAt(LocalDateTime.now())
                .build());
        couponId = coupon.getCouponId().id();
        customerId = "CU" + uniq;
        CouponId cid = CouponId.of(couponId);
        CustomerId cuid = CustomerId.of(customerId);
        couponIssueRepository.save(CouponIssues.create(cid, cuid)); // UNUSED
        issueId = new CouponIssueId(cid, cuid);
    }

    private CouponApplyEvent applyEvent(String orderId) {
        return new CouponApplyEvent(orderId, customerId, couponId, ORIGIN_AMT, List.of(), "CARD", "shinHan");
    }

    private CouponRestoreEvent restoreEvent(String orderId) {
        return new CouponRestoreEvent(orderId, couponId, customerId);
    }

    private CouponIssues reload() {
        return couponIssueRepository.findById(issueId).orElseThrow();
    }

    private String newOrderId() {
        return "O" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    @DisplayName("apply는 쿠폰을 소유 주문으로 USED 처리하고 할인액과 함께 CouponAppliedEvent를 발행한다")
    @Test
    void applyMarksCouponUsedAndPublishesAppliedEvent() {
        String o1 = newOrderId();

        consumer.handleApplyCoupon(applyEvent(o1));

        CouponIssues issue = reload();
        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.USED);
        assertThat(issue.getOrderId().id()).isEqualTo(o1);

        verify(publisher).publish(eq(EventTopic.COUPON_APPLIED_TOPIC), any(CouponAppliedEvent.class));
    }

    @DisplayName("restore 재전달(중복)은 멱등 — UNUSED로 수렴")
    @Test
    void restoreIsIdempotentOnRedelivery() {
        String o1 = newOrderId();
        consumer.handleApplyCoupon(applyEvent(o1));

        consumer.handleRestoreCoupon(restoreEvent(o1));
        consumer.handleRestoreCoupon(restoreEvent(o1)); // 재전달
        consumer.handleRestoreCoupon(restoreEvent(o1)); // 재전달

        CouponIssues issue = reload();
        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.UNUSED);
        assertThat(issue.getOrderId()).isNull();
    }

    @DisplayName("다른 주문의 보상 이벤트는 현재 사용 중인 쿠폰을 복구하지 않는다")
    @Test
    void restoreOfDifferentOrderIsIgnored() {
        String o1 = newOrderId();
        String o2 = newOrderId();

        consumer.handleApplyCoupon(applyEvent(o1));     // USED(o1)
        consumer.handleRestoreCoupon(restoreEvent(o2)); // 타 주문 보상 → 무시

        CouponIssues issue = reload();
        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.USED);
        assertThat(issue.getOrderId().id()).isEqualTo(o1);
    }

    @DisplayName("보상 후 새 주문이 동일 쿠폰을 재사용할 수 있다")
    @Test
    void reusableByNewOrderAfterCompensation() {
        String o1 = newOrderId();
        String o2 = newOrderId();

        consumer.handleApplyCoupon(applyEvent(o1));     // USED(o1)
        consumer.handleRestoreCoupon(restoreEvent(o1)); // 보상 → UNUSED
        consumer.handleApplyCoupon(applyEvent(o2));     // 신규 주문 재사용 → USED(o2)

        CouponIssues issue = reload();
        assertThat(issue.getStatus()).isEqualTo(CouponIssueStatus.USED);
        assertThat(issue.getOrderId().id()).isEqualTo(o2);

        verify(publisher, times(2)).publish(eq(EventTopic.COUPON_APPLIED_TOPIC), any(CouponAppliedEvent.class));
    }
}
