package com.commerce.payments.application.service;

import com.commerce.payments.core.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.application.port.out.PaymentOutPort;
import com.commerce.payments.core.application.port.out.PgStrategy;
import com.commerce.payments.core.domain.aggregate.Payment;
import com.commerce.payments.core.domain.enums.PayMethod;
import com.commerce.payments.core.domain.enums.PaymentStatus;
import com.commerce.payments.core.domain.enums.PgProvider;
import com.commerce.payments.core.domain.service.PaymentPgRouter;
import com.commerce.payments.core.domain.service.PaymentUseCaseImpl;
import com.commerce.payments.core.domain.vo.payments.PgPayCancelResponse;
import com.commerce.payments.core.domain.vo.payments.PgPayResponse;
import com.commerce.payments.infrastructure.adaptor.PaymentAdaptor;
import com.commerce.payments.infrastructure.persistence.PaymentPartCancelRepository;
import com.commerce.shared.enums.PayProvider;
import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import com.commerce.shared.vo.PaymentId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        PaymentUseCaseImpl.class,
        PaymentAdaptor.class,
})
@DataJpaTest
class PaymentUseCaseImplTest {

    @Autowired
    private PaymentUseCaseImpl paymentUseCase;

    @Autowired
    private PaymentOutPort paymentOutPort;

    @Autowired
    private PaymentPartCancelRepository paymentPartCancelRepository;

    @MockitoBean
    private PaymentPgRouter mockPaymentPgRouter;


//    private final ProductId productId1 = ProductId.of("P20251110222600000001");
//    private final ProductId productId2 = ProductId.of("P20251110222600000002");
    private final OrderId testOrderId = OrderId.create();
    private PaymentId approved_paymentId;

    private PgPayResponse success_pgResponse = PgPayResponse.builder()
            .pgTid("PG_TID_12345")
            .responseCode("0000")
            .responseMessage("성공")
            .amount(Money.of(50000L))
            .isSuccess(true)
            .build();

    private PgPayCancelResponse cancelResponse = PgPayCancelResponse.builder()
            .pgCcTid("PG_CC_TID_12345")
            .responseCode("success")
            .responseMessage("취소성공")
            .cancelReason("고객취소")
            .cancelAmount(60000L)
            .isSuccess(true)
            .build();

    private PgPayResponse fail_pgResponse = PgPayResponse.builder()
            .pgTid("PG_TID_12345")
            .responseCode("errorCode")
            .responseMessage("실패")
            .isSuccess(false)
            .build();

    @DisplayName("주문 결제 성공")
    @Test
    void doApproval_successful() {
        PayOrderCommand command = PayOrderCommand.builder()
                .orderId(testOrderId)
                .approvedAmount(Money.of(50000))
                .installment(3)
                .payMethod(PayMethod.CARD)
                .payProvider(PayProvider.KB)
                .build();

        mockPgStrategy();

        paymentUseCase.doApproval(command);

        // then 승인 성공
        Payment payment = paymentOutPort.findByOrderId(testOrderId)
                .get();
        assertThat(payment).isNotNull();
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
        approved_paymentId = payment.getPaymentId();
    }

    @DisplayName("전체환불 성공")
    @Test
    void doCancel_successful() {
        // 주문 결제
        doApproval_successful();

        // 전체취소 요청
        PayCancelCommand command = PayCancelCommand.builder()
                .orderId(testOrderId)
                .canceledAmount(Money.of(50000))
                .paymentStatus(PaymentStatus.FULL_CANCELED)
                .build();

        mockPgStrategy();

        paymentUseCase.doCancel(command);

        // payment 상태
        assertThat(paymentOutPort.findByOrderId(testOrderId)).isNotNull();
        assertThat(paymentOutPort.findByOrderId(testOrderId).get().getPaymentStatus())
                .isEqualTo(PaymentStatus.FULL_CANCELED);
    }

    @DisplayName("전체취소실패 : 부분취소내역 존재")
    @Test
    void doCancel_failed() {
        // 결제 승인
        doApproval_successful();

        mockPgStrategy();

        // 부분취소 요청
        PayCancelCommand partCancelCommand = PayCancelCommand.builder()
                .orderId(testOrderId)
                .canceledAmount(Money.of(30000))
                .paymentStatus(PaymentStatus.PARTIAL_CANCELED)
                .build();

        mockPgStrategy();

        // 부분취소
        paymentUseCase.doCancel(partCancelCommand);

        // 전체취소
        PayCancelCommand fullCancelCommand = PayCancelCommand.builder()
                .orderId(testOrderId)
                .paymentStatus(PaymentStatus.FULL_CANCELED)
                .build();

        // 전체취소 실패
        assertThatThrownBy(() -> paymentUseCase.doCancel(fullCancelCommand))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.PAYMENT_HAS_PARTIAL_CANCEL.getMessage());

        // payment 상태 검증
        Payment payment = paymentOutPort.findByOrderId(testOrderId).orElseThrow();
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.APPROVED);
    }


    @DisplayName("부분취소 성공")
    @Test
    void doPartCancel_successful() {
        // 주문 결제
        doApproval_successful();

        // 부분취소 요청
        PayCancelCommand partCancelCommand = PayCancelCommand.builder()
                .orderId(testOrderId)
                .canceledAmount(Money.of(30000))
                .paymentStatus(PaymentStatus.PARTIAL_CANCELED)
                .build();

        mockPgStrategy();

        // 부분취소
        paymentUseCase.doCancel(partCancelCommand);

        // 원거래 상태
        assertThat(paymentOutPort.findByOrderId(testOrderId).get().getPaymentStatus())
                .as("원거래는 승인상태 유지")
                .isEqualTo(PaymentStatus.APPROVED);

        // 부분취소거래 검증
        Money remainAmt = paymentPartCancelRepository.selectRemainAmountByPaymentId(approved_paymentId);

        assertThat(remainAmt.value())
                .as("부분취소 금액 검증")
                .isEqualTo(50000 - 30000);

        // orderitem 검증
      /*  assertThat(orderItemRepository.findById(canceledItem.getId()))
                .isPresent()
                .get()
                .satisfies(
                        orderItem -> {
                            assertThat(orderItem.isCanceled()).isEqualTo(true);
                        }
                );

        assertThat(orderItemRepository.findByOrderIdAndProductIdAndCanceled(partCancelCommand.getOrderId(), canceledItem.getProductId(), false))
                .isPresent()
                .get()
                .satisfies(oi -> {
                    assertThat(oi.getQuantity().value())
                            .as("새로 추가된 orderitem 수량 확인")
                            .isEqualTo(canceledItem.getQuantity().minus(partCancelCommand.getCanceledQuantity()).value());
                    assertThat(oi.isCanceled()).isEqualTo(false);
                });*/
    }

    @DisplayName("부분취소 실패 : 취소가능 금액 초과")
    @Test
    void doPartCancel_failed() {
        // 주문 결제
        doApproval_successful();

        // 부분취소 요청
        PayCancelCommand partCancelCommand = PayCancelCommand.builder()
                .orderId(testOrderId)
                .canceledAmount(Money.of(60000))
                .paymentStatus(PaymentStatus.PARTIAL_CANCELED)
                .build();

        mockPgStrategy();

        // 부분취소
        assertThatThrownBy(() -> paymentUseCase.doCancel(partCancelCommand))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.PAYMENT_CANCEL_AMOUNT_EXCEEDED.getMessage());

        assertThat(paymentOutPort.findByOrderId(testOrderId).get().getPaymentStatus())
                .as("원거래는 승인상태 유지")
                .isEqualTo(PaymentStatus.APPROVED);

        PaymentId paymentId = paymentOutPort.findByOrderId(testOrderId).get().getPaymentId();
        assertThat(paymentPartCancelRepository.existsPaymentPartCancelByApprovedPaymentId(paymentId))
                .as("not insert 부분취소")
                .isEqualTo(false);
    }

    private void mockPgStrategy() {
        PgStrategy mockPgStrategy = mock(PgStrategy.class);

        when(mockPaymentPgRouter.routePg(any(PayMethod.class), any(PayProvider.class)))
                .thenReturn(mockPgStrategy);

        when(mockPaymentPgRouter.getPgStrategyByProvider(any(), any()))
                .thenReturn(mockPgStrategy);

        when(mockPgStrategy.getPgProvider())
                .thenReturn(PgProvider.TOSS);

        when(mockPgStrategy.processApproval(any()))
                .thenReturn(success_pgResponse);

        when(mockPgStrategy.processCancel(any()))
                .thenReturn(cancelResponse);
    }
}