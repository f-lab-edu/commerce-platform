package com.commerce.payments.application.service;

import com.commerce.payments.core.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.application.port.out.PaymentOutPort;
import com.commerce.payments.core.application.port.out.PgStrategy;
import com.commerce.payments.core.domain.enums.PayMethod;
import com.commerce.payments.core.domain.enums.PaymentStatus;
import com.commerce.payments.core.domain.enums.PgApprovalStatus;
import com.commerce.payments.core.domain.enums.PgProvider;
import com.commerce.payments.core.domain.service.PaymentPgRouter;
import com.commerce.payments.core.domain.service.PaymentUseCaseImpl;
import com.commerce.payments.core.domain.vo.payments.PgApprovalResult;
import com.commerce.payments.core.domain.vo.payments.PgPayCancelResponse;
import com.commerce.payments.core.domain.vo.payments.PgPayResponse;
import com.commerce.shared.enums.PayProvider;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * PaymentUseCaseImpl.approveWithPg 단위테스트 (PG 단계)
 */
@ExtendWith(MockitoExtension.class)
class PaymentApproveWithPgTest {

    @Mock PaymentPgRouter pgRouter;
    @Mock PaymentOutPort paymentOutPort;
    @Mock PgStrategy pgStrategy;

    PaymentUseCaseImpl paymentUseCase;

    private final PayOrderCommand command = PayOrderCommand.builder()
            .orderId(OrderId.create())
            .approvedAmount(Money.of(50000))
            .payMethod(PayMethod.CARD)
            .payProvider(PayProvider.KB)
            .build();

    @BeforeEach
    void setUp() {
        paymentUseCase = new PaymentUseCaseImpl(pgRouter, paymentOutPort);
        lenient().when(pgRouter.routePg(PayMethod.CARD, PayProvider.KB)).thenReturn(pgStrategy);
        lenient().when(pgStrategy.getPgProvider()).thenReturn(PgProvider.TOSS);
    }

    @DisplayName("PG 라우팅 실패 → PG 미호출 DECLINED (예외 전파 없음)")
    @Test
    void routingFailed_declined() {
        given(pgRouter.routePg(PayMethod.CARD, PayProvider.KB)).willThrow(new NoSuchElementException());

        PgApprovalResult result = paymentUseCase.approveWithPg(command);

        assertThat(result.status()).isEqualTo(PgApprovalStatus.DECLINED);
        assertThat(result.pgProvider()).isNull();
        assertThat(result.reason()).contains("PG 라우팅 실패");
        verifyNoInteractions(pgStrategy);
    }

    @DisplayName("실패거래 저장: 같은 orderId 실패거래가 없으면 저장")
    @Test
    void savePaymentFailure_saves() {
        given(paymentOutPort.existsPaymentFailureByOrderId(command.getOrderId())).willReturn(false);

        paymentUseCase.savePaymentFailure(command.getOrderId(), declinedResult());

        verify(paymentOutPort).savePaymentFailure(any());
    }

    @DisplayName("실패거래 저장: pg-result 재수신으로 이미 저장된 orderId면 중복 저장하지 않음")
    @Test
    void savePaymentFailure_idempotent() {
        given(paymentOutPort.existsPaymentFailureByOrderId(command.getOrderId())).willReturn(true);

        paymentUseCase.savePaymentFailure(command.getOrderId(), declinedResult());

        verify(paymentOutPort, never()).savePaymentFailure(any());
    }

    private PgApprovalResult declinedResult() {
        return PgApprovalResult.builder()
                .status(PgApprovalStatus.DECLINED)
                .reason("한도초과")
                .build();
    }

    @DisplayName("승인 성공 + 금액 일치 → APPROVED, 망취소·DB 저장 없음")
    @Test
    void approved() {
        given(pgStrategy.processApproval(command)).willReturn(approvalResponse(true, 50000L));

        PgApprovalResult result = paymentUseCase.approveWithPg(command);

        assertThat(result.status()).isEqualTo(PgApprovalStatus.APPROVED);
        assertThat(result.pgTid()).isEqualTo("PG_TID");
        assertThat(result.pgApprovedAmt()).isEqualTo(50000L);
        assertThat(result.approvedAt()).isNotNull();
        verify(pgStrategy, never()).processCancel(any());
        verifyNoInteractions(paymentOutPort);
    }

    @DisplayName("승인 거절 → DECLINED, 망취소 없음")
    @Test
    void declined() {
        given(pgStrategy.processApproval(command)).willReturn(PgPayResponse.builder()
                .responseCode("errorCode")
                .responseMessage("한도초과")
                .isSuccess(false)
                .build());

        PgApprovalResult result = paymentUseCase.approveWithPg(command);

        assertThat(result.status()).isEqualTo(PgApprovalStatus.DECLINED);
        assertThat(result.responseCode()).isEqualTo("errorCode");
        assertThat(result.reason()).isEqualTo("한도초과");
        verify(pgStrategy, never()).processCancel(any());
    }

    @DisplayName("승인 성공 + 금액 불일치 → PG 승인금액 전액 망취소 성공 → AMOUNT_MISMATCH_VOIDED")
    @Test
    void amountMismatch_voided() {
        given(pgStrategy.processApproval(command)).willReturn(approvalResponse(true, 40000L));
        given(pgStrategy.getVoidTid(command, "PG_TID")).willReturn("PG_TID");
        given(pgStrategy.processCancel(any())).willReturn(cancelResponse(true));

        PgApprovalResult result = paymentUseCase.approveWithPg(command);

        assertThat(result.status()).isEqualTo(PgApprovalStatus.AMOUNT_MISMATCH_VOIDED);
        assertThat(result.pgCancelTid()).isEqualTo("PG_CC_TID");
        assertThat(result.canceledAt()).isNotNull();
        assertThat(result.reason()).contains("요청금액 불일치");

        ArgumentCaptor<PayCancelCommand> captor = ArgumentCaptor.forClass(PayCancelCommand.class);
        verify(pgStrategy).processCancel(captor.capture());
        assertThat(captor.getValue().getPgTid()).isEqualTo("PG_TID");
        assertThat(captor.getValue().getPaymentStatus()).isEqualTo(PaymentStatus.FULL_CANCELED);
        assertThat(captor.getValue().getCanceledAmount()).isEqualTo(Money.of(40000L));
    }

    @DisplayName("승인 성공 + 금액 불일치 → 망취소 응답 실패 → VOID_FAILED")
    @Test
    void amountMismatch_voidFailed() {
        given(pgStrategy.processApproval(command)).willReturn(approvalResponse(true, 40000L));
        given(pgStrategy.getVoidTid(command, "PG_TID")).willReturn("PG_TID");
        given(pgStrategy.processCancel(any())).willReturn(cancelResponse(false));

        PgApprovalResult result = paymentUseCase.approveWithPg(command);

        assertThat(result.status()).isEqualTo(PgApprovalStatus.VOID_FAILED);
        assertThat(result.pgTid()).isEqualTo("PG_TID");
        assertThat(result.reason()).contains("망취소 실패");
    }

    @DisplayName("승인 성공 + 금액 불일치 → 망취소 예외 → 예외 전파 없이 VOID_FAILED")
    @Test
    void amountMismatch_voidException() {
        given(pgStrategy.processApproval(command)).willReturn(approvalResponse(true, 40000L));
        given(pgStrategy.getVoidTid(command, "PG_TID")).willReturn("PG_TID");
        given(pgStrategy.processCancel(any())).willThrow(new ResourceAccessException("timeout"));

        PgApprovalResult result = paymentUseCase.approveWithPg(command);

        assertThat(result.status()).isEqualTo(PgApprovalStatus.VOID_FAILED);
        assertThat(result.pgTid()).isEqualTo("PG_TID");
    }

    @DisplayName("결과 불명(PG 호출 예외) + 거래키 특정 가능 → 망취소 성공 → UNKNOWN_VOIDED")
    @Test
    void unknown_voided() {
        given(pgStrategy.processApproval(command)).willThrow(new ResourceAccessException("timeout"));
        given(pgStrategy.getVoidTid(command, null)).willReturn("PAYMENT_KEY");
        given(pgStrategy.processCancel(any())).willReturn(cancelResponse(true));

        PgApprovalResult result = paymentUseCase.approveWithPg(command);

        assertThat(result.status()).isEqualTo(PgApprovalStatus.UNKNOWN_VOIDED);
        assertThat(result.pgTid()).isEqualTo("PAYMENT_KEY");
        assertThat(result.pgApprovedAmt()).isNull();
    }

    @DisplayName("결과 불명(PG 호출 예외) + 거래키 없음 → 망취소 미시도 VOID_FAILED")
    @Test
    void unknown_noVoidTid() {
        given(pgStrategy.processApproval(command)).willThrow(new ResourceAccessException("timeout"));
        given(pgStrategy.getVoidTid(command, null)).willReturn(null);

        PgApprovalResult result = paymentUseCase.approveWithPg(command);

        assertThat(result.status()).isEqualTo(PgApprovalStatus.VOID_FAILED);
        verify(pgStrategy, never()).processCancel(any());
    }

    @DisplayName("기존 doApproval: 금액 불일치는 예외 대신 망취소 후 거래실패 저장")
    @Test
    void doApproval_amountMismatch_savesFailure() {
        given(pgStrategy.processApproval(command)).willReturn(approvalResponse(true, 40000L));
        given(pgStrategy.getVoidTid(command, "PG_TID")).willReturn("PG_TID");
        given(pgStrategy.processCancel(any())).willReturn(cancelResponse(true));

        paymentUseCase.doApproval(command);

        verify(paymentOutPort).savePaymentFailure(any());
        verify(paymentOutPort, never()).savePayment(any());
    }

    private PgPayResponse approvalResponse(boolean success, long amount) {
        return PgPayResponse.builder()
                .pgTid("PG_TID")
                .responseCode("0000")
                .responseMessage("성공")
                .amount(Money.of(amount))
                .isSuccess(success)
                .build();
    }

    private PgPayCancelResponse cancelResponse(boolean success) {
        return PgPayCancelResponse.builder()
                .pgCcTid("PG_CC_TID")
                .responseCode(success ? "0000" : "9999")
                .isSuccess(success)
                .build();
    }
}
