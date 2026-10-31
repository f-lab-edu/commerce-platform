package com.commerce.payments.bootstrap.event;

import com.commerce.payments.core.application.port.in.PaymentUseCase;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.application.port.out.PaymentOutPort;
import com.commerce.payments.core.domain.service.PaymentPgRouter;
import com.commerce.payments.core.domain.service.PaymentUseCaseImpl;
import com.commerce.payments.core.domain.enums.PayMethod;
import com.commerce.payments.core.domain.enums.PgApprovalStatus;
import com.commerce.payments.core.domain.enums.PgProvider;
import com.commerce.payments.core.domain.vo.payments.PgApprovalResult;
import com.commerce.shared.enums.PayProvider;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.CouponAppliedEvent;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.PaymentPgResultEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.kafka.event.type.PgResult;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentPgConsumerTest {

    @Mock PaymentUseCase paymentUseCase;
    @Mock TransactionalEventPublisher publisher;

    PaymentPgConsumer consumer;

    private final String orderId = "O-test-order";
    private final CouponAppliedEvent event = new CouponAppliedEvent(
            orderId, "C001", "CP001",
            List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(2))),
            "CARD", "shinHan", 0,
            10000L, 1000L,
            orderId, LocalDateTime.now());

    @BeforeEach
    void setUp() {
        consumer = new PaymentPgConsumer(paymentUseCase, publisher);
    }

    @DisplayName("승인요청금액 = originAmt - discountAmt, 결과를 payment.pg-result 로 발행한다")
    @Test
    void publishesPgResult() {
        given(paymentUseCase.approveWithPg(any())).willReturn(PgApprovalResult.builder()
                .status(PgApprovalStatus.APPROVED)
                .pgProvider(PgProvider.TOSS)
                .pgTid("PG_TID")
                .pgApprovedAmt(9000L)
                .approvedAt(LocalDateTime.now())
                .build());

        consumer.onCouponApplied(event);

        ArgumentCaptor<PayOrderCommand> commandCaptor = ArgumentCaptor.forClass(PayOrderCommand.class);
        verify(paymentUseCase).approveWithPg(commandCaptor.capture());
        assertThat(commandCaptor.getValue().getApprovedAmount().value()).isEqualTo(9000L);
        assertThat(commandCaptor.getValue().getPayMethod()).isEqualTo(PayMethod.CARD);
        assertThat(commandCaptor.getValue().getPayProvider()).isEqualTo(PayProvider.SHIN_HAN);

        ArgumentCaptor<PaymentPgResultEvent> eventCaptor = ArgumentCaptor.forClass(PaymentPgResultEvent.class);
        verify(publisher).publish(eq(EventTopic.PAYMENT_PG_RESULT_TOPIC), eventCaptor.capture());
        PaymentPgResultEvent published = eventCaptor.getValue();
        assertThat(published.result()).isEqualTo(PgResult.APPROVED);
        assertThat(published.requestedAmt()).isEqualTo(9000L);
        assertThat(published.pgProvider()).isEqualTo("TOSS");
        assertThat(published.items()).isEqualTo(event.items());
        assertThat(published.couponId()).isEqualTo("CP001");
        assertThat(published.key()).isEqualTo(orderId);
    }

    @DisplayName("망취소 실패 결과도 예외 없이 VOID_FAILED 로 발행한다")
    @Test
    void publishesVoidFailed() {
        given(paymentUseCase.approveWithPg(any())).willReturn(PgApprovalResult.builder()
                .status(PgApprovalStatus.VOID_FAILED)
                .pgProvider(PgProvider.NHN)
                .reason("승인 결과 불명 / 망취소 불가")
                .build());

        consumer.onCouponApplied(event);

        ArgumentCaptor<PaymentPgResultEvent> eventCaptor = ArgumentCaptor.forClass(PaymentPgResultEvent.class);
        verify(publisher).publish(eq(EventTopic.PAYMENT_PG_RESULT_TOPIC), eventCaptor.capture());
        assertThat(eventCaptor.getValue().result()).isEqualTo(PgResult.VOID_FAILED);
        assertThat(eventCaptor.getValue().reason()).contains("망취소 불가");
    }

    @DisplayName("변환 불가 payMethod → PG 미호출, DECLINED 로 pg-result 발행")
    @Test
    void invalidPayMethod_publishesDeclined() {
        consumer.onCouponApplied(couponApplied("easyPay", "shinHan", 10000L, 1000L));

        verify(paymentUseCase, never()).approveWithPg(any());
        PaymentPgResultEvent published = capturePgResult();
        assertThat(published.result()).isEqualTo(PgResult.DECLINED);
        assertThat(published.pgProvider()).isNull();
        assertThat(published.pgTid()).isNull();
        assertThat(published.reason()).contains("결제 요청값 오류");
    }

    @DisplayName("결제금액(originAmt - discountAmt) 음수 → PG 미호출, DECLINED 로 pg-result 발행")
    @Test
    void negativeAmount_publishesDeclined() {
        consumer.onCouponApplied(couponApplied("CARD", "shinHan", 1000L, 2000L));

        verify(paymentUseCase, never()).approveWithPg(any());
        PaymentPgResultEvent published = capturePgResult();
        assertThat(published.result()).isEqualTo(PgResult.DECLINED);
        assertThat(published.reason()).contains("결제금액 음수");
    }

    @DisplayName("PG 라우팅 실패(routePg 예외) → 예외 전파 없이 DECLINED 로 pg-result 발행")
    @Test
    void routingFailed_publishesDeclined() {
        PaymentPgRouter pgRouter = mock(PaymentPgRouter.class);
        given(pgRouter.routePg(any(), any())).willThrow(new NoSuchElementException("사용 가능한 PG 없음"));
        PaymentPgConsumer consumerWithRealUseCase = new PaymentPgConsumer(
                new PaymentUseCaseImpl(pgRouter, mock(PaymentOutPort.class)), publisher);

        consumerWithRealUseCase.onCouponApplied(event);

        PaymentPgResultEvent published = capturePgResult();
        assertThat(published.result()).isEqualTo(PgResult.DECLINED);
        assertThat(published.pgProvider()).isNull();
        assertThat(published.reason()).contains("PG 라우팅 실패");
    }

    private CouponAppliedEvent couponApplied(String payMethod, String payProvider, long originAmt, long discountAmt) {
        return new CouponAppliedEvent(
                orderId, "C001", "CP001", event.items(),
                payMethod, payProvider, 0,
                originAmt, discountAmt,
                orderId, LocalDateTime.now());
    }

    private PaymentPgResultEvent capturePgResult() {
        ArgumentCaptor<PaymentPgResultEvent> eventCaptor = ArgumentCaptor.forClass(PaymentPgResultEvent.class);
        verify(publisher).publish(eq(EventTopic.PAYMENT_PG_RESULT_TOPIC), eventCaptor.capture());
        return eventCaptor.getValue();
    }
}
