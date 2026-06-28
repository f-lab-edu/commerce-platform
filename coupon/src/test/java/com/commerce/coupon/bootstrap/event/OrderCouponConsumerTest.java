package com.commerce.coupon.bootstrap.event;

import com.commerce.coupon.bootstrap.event.dto.CouponApplyEvent;
import com.commerce.coupon.bootstrap.event.dto.CouponRestoreEvent;
import com.commerce.coupon.core.application.port.in.CouponIssueUseCase;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.CouponAppliedEvent;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCouponConsumerTest {

    @Mock CouponIssueUseCase couponIssueUseCase;
    @Mock TransactionalEventPublisher transactionalEventPublisher;

    OrderCouponConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderCouponConsumer(couponIssueUseCase, transactionalEventPublisher);
    }

    @DisplayName("couponId가 null이면 discountAmt=0으로 CouponAppliedEvent를 발행한다")
    @Test
    void handleNoCoupon() {
        given(couponIssueUseCase.applyCouponForSaga(null, "C001", "O001", 10000)).willReturn(0L);

        CouponApplyEvent event = new CouponApplyEvent(
                "O001", "C001", null, 10000L,
                List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(2))),
                "CARD", "shinHan"
        );

        consumer.handleApplyCoupon(event);

        verify(transactionalEventPublisher).publish(eq(EventTopic.COUPON_APPLIED_TOPIC), any(CouponAppliedEvent.class));
    }

    @DisplayName("보상 이벤트에 couponId 없으면 restoreCouponForSaga에 null 전달")
    @Test
    void handleCompensationNoCoupon() {
        CouponRestoreEvent event = new CouponRestoreEvent("O001", null, null);

        consumer.handleRestoreCoupon(event);

        verify(couponIssueUseCase).restoreCouponForSaga(null, null);
    }
}
