package com.commerce.payments.bootstrap.event;

import com.commerce.payments.core.application.port.in.PaymentUseCase;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.domain.aggregate.Payment;
import com.commerce.payments.core.domain.enums.PgApprovalStatus;
import com.commerce.payments.core.domain.enums.PgProvider;
import com.commerce.payments.core.domain.vo.payments.PgApprovalResult;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.PaymentCompletedEvent;
import com.commerce.shared.kafka.event.dto.PaymentFailedEvent;
import com.commerce.shared.kafka.event.dto.PaymentPgResultEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.kafka.event.type.PgResult;
import com.commerce.shared.vo.OrderId;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentDbConsumerTest {

    @Mock PaymentUseCase paymentUseCase;
    @Mock TransactionalEventPublisher publisher;

    PaymentDbConsumer consumer;

    private final String orderId = "O-test-order";
    private final List<ItemEntry> items = List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(2)));

    @BeforeEach
    void setUp() {
        consumer = new PaymentDbConsumer(paymentUseCase, publisher);
    }

    @DisplayName("APPROVED → payment 저장 후 payment.completed 발행")
    @Test
    void approved_publishesCompleted() {
        PaymentPgResultEvent event = pgResultEvent(PgResult.APPROVED, null);
        Payment payment = approvedPayment();
        given(paymentUseCase.saveApprovedPayment(any(), any())).willReturn(payment);

        consumer.onPgResult(event);

        ArgumentCaptor<PayOrderCommand> commandCaptor = ArgumentCaptor.forClass(PayOrderCommand.class);
        ArgumentCaptor<PgApprovalResult> resultCaptor = ArgumentCaptor.forClass(PgApprovalResult.class);
        verify(paymentUseCase).saveApprovedPayment(commandCaptor.capture(), resultCaptor.capture());
        assertThat(commandCaptor.getValue().getApprovedAmount().value()).isEqualTo(9000L);
        assertThat(resultCaptor.getValue().status()).isEqualTo(PgApprovalStatus.APPROVED);
        assertThat(resultCaptor.getValue().pgTid()).isEqualTo("PG_TID");

        ArgumentCaptor<PaymentCompletedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);
        verify(publisher).publish(eq(EventTopic.PAYMENT_COMPLETED_TOPIC), eventCaptor.capture());
        PaymentCompletedEvent published = eventCaptor.getValue();
        assertThat(published.orderId()).isEqualTo(orderId);
        assertThat(published.originAmt()).isEqualTo(10000L);
        assertThat(published.discountAmt()).isEqualTo(1000L);
        assertThat(published.paymentId()).isEqualTo(payment.getPaymentId().id());
        assertThat(published.approvedAmt()).isEqualTo(9000L);
        verify(paymentUseCase, never()).savePaymentFailure(any(), any());
    }

    @DisplayName("APPROVED 외 결과 → payment_failure 저장 후 reason 포함 payment.failed 발행")
    @Test
    void failure_publishesFailed() {
        PaymentPgResultEvent event = pgResultEvent(PgResult.AMOUNT_MISMATCH_VOIDED, "요청금액 불일치");

        consumer.onPgResult(event);

        ArgumentCaptor<PgApprovalResult> resultCaptor = ArgumentCaptor.forClass(PgApprovalResult.class);
        verify(paymentUseCase).savePaymentFailure(eq(new OrderId(orderId)), resultCaptor.capture());
        assertThat(resultCaptor.getValue().status()).isEqualTo(PgApprovalStatus.AMOUNT_MISMATCH_VOIDED);

        ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(publisher).publish(eq(EventTopic.PAYMENT_FAILED_TOPIC), eventCaptor.capture());
        PaymentFailedEvent published = eventCaptor.getValue();
        assertThat(published.reason()).isEqualTo("요청금액 불일치");
        assertThat(published.items()).isEqualTo(items);
        assertThat(published.couponId()).isEqualTo("CP001");
        assertThat(published.customerId()).isEqualTo("C001");
        verify(paymentUseCase, never()).saveApprovedPayment(any(), any());
        verify(publisher, never()).publish(eq(EventTopic.PAYMENT_COMPLETED_TOPIC), any());
    }

    @DisplayName("DB 저장 실패 시 예외 전파(재시도), 이벤트 미발행")
    @Test
    void saveFailure_throwsWithoutPublish() {
        PaymentPgResultEvent event = pgResultEvent(PgResult.DECLINED, "한도초과");
        doThrow(new RuntimeException("db down"))
                .when(paymentUseCase).savePaymentFailure(any(), any());

        assertThatThrownBy(() -> consumer.onPgResult(event))
                .isInstanceOf(RuntimeException.class);
        verify(publisher, never()).publish(any(), any());
    }

    private PaymentPgResultEvent pgResultEvent(PgResult result, String reason) {
        boolean approved = result == PgResult.APPROVED;
        return new PaymentPgResultEvent(
                orderId, "C001", "CP001",
                items, "CARD", "shinHan", 0,
                10000L, 1000L,
                result, reason, 9000L, approved ? 9000L : null,
                "TOSS", "0000", "PG_TID", null,
                approved ? LocalDateTime.now() : null, null,
                orderId, LocalDateTime.now());
    }

    private Payment approvedPayment() {
        PayOrderCommand command = PayOrderCommand.of(orderId, 9000L, "CARD", "shinHan", 0);
        Payment payment = Payment.create(command, PgProvider.TOSS);
        payment.approved(PgApprovalResult.builder()
                .status(PgApprovalStatus.APPROVED)
                .pgTid("PG_TID")
                .approvedAt(LocalDateTime.now())
                .build());
        return payment;
    }
}
