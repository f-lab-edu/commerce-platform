package com.commerce.payments.core.domain.service;

import com.commerce.payments.core.application.port.in.PaymentUseCase;
import com.commerce.payments.core.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.application.port.out.PaymentOutPort;
import com.commerce.payments.core.application.port.out.PgStrategy;
import com.commerce.payments.core.domain.aggregate.Payment;
import com.commerce.payments.core.domain.aggregate.PaymentFailure;
import com.commerce.payments.core.domain.aggregate.PaymentPartCancel;
import com.commerce.payments.core.domain.enums.PaymentStatus;
import com.commerce.payments.core.domain.enums.PgApprovalStatus;
import com.commerce.payments.core.domain.vo.payments.PgApprovalResult;
import com.commerce.payments.core.domain.vo.payments.PgPayCancelResponse;
import com.commerce.payments.core.domain.vo.payments.PgPayResponse;
import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.commerce.shared.exception.BusinessError.*;

@Log4j2
@RequiredArgsConstructor
@Service
public class PaymentUseCaseImpl implements PaymentUseCase {
    private final PaymentPgRouter pgRouter;
    private final PaymentOutPort paymentOutPort;

    @Override
    @Transactional
    public void doApproval(PayOrderCommand payOrdercommand) {
        PgApprovalResult approvalResult = approveWithPg(payOrdercommand);

        if (approvalResult.isApproved()) {
            saveApprovedPayment(payOrdercommand, approvalResult);
        } else {
            savePaymentFailure(payOrdercommand.getOrderId(), approvalResult);
        }
    }

    /**
     * PG 승인
     * 예외를 던지지 않고 결과로 반환한다. (호출자 재시도 시 이중 승인 방지)
     * - PG 호출 전 실패(라우팅 실패): DECLINED (PG 미호출)
     */
    @Override
    public PgApprovalResult approveWithPg(PayOrderCommand command) {
        // pg사 라우팅
        PgStrategy pgStrategy;
        try {
            pgStrategy = pgRouter.routePg(command.getPayMethod(), command.getPayProvider());
        } catch (RuntimeException e) {
            log.warn("PG 라우팅 실패 (PG 미호출) - orderId: {}", command.getOrderId(), e);
            return PgApprovalResult.builder()
                    .status(PgApprovalStatus.DECLINED)
                    .reason("PG 라우팅 실패: " + e.getMessage())
                    .build();
        }

        PgPayResponse pgResponse;
        try {
            pgResponse = pgStrategy.processApproval(command);
        } catch (Exception e) {
            log.error("PG 승인 결과 불명 - orderId: {}", command.getOrderId(), e);
            return voidApproval(pgStrategy, command, null, null,
                    PgApprovalStatus.UNKNOWN_VOIDED, "승인 결과 불명: " + e.getMessage());
        }

        if (pgResponse == null) {
            return voidApproval(pgStrategy, command, null, null,
                    PgApprovalStatus.UNKNOWN_VOIDED, "승인 결과 불명: PG 응답 없음");
        }

        // 승인거절
        if (!pgResponse.isSuccess()) {
            log.info("PG 승인 거절 - orderId: {}, errorCode: {}", command.getOrderId(), pgResponse.responseCode());
            return PgApprovalResult.builder()
                    .status(PgApprovalStatus.DECLINED)
                    .pgProvider(pgStrategy.getPgProvider())
                    .pgTid(pgResponse.pgTid())
                    .responseCode(pgResponse.responseCode())
                    .reason(pgResponse.responseMessage())
                    .build();
        }

        // 승인성공 + 요청금액 불일치
        Long pgApprovedAmt = pgResponse.amount() == null ? null : pgResponse.amount().value();
        if (!Objects.equals(command.getApprovedAmount().value(), pgApprovedAmt)) {
            return voidApproval(pgStrategy, command, pgResponse.pgTid(), pgApprovedAmt,
                    PgApprovalStatus.AMOUNT_MISMATCH_VOIDED,
                    "요청금액 불일치 (요청: " + command.getApprovedAmount().value() + ", PG: " + pgApprovedAmt + ")");
        }

        return PgApprovalResult.builder()
                .status(PgApprovalStatus.APPROVED)
                .pgProvider(pgStrategy.getPgProvider())
                .pgTid(pgResponse.pgTid())
                .pgApprovedAmt(pgApprovedAmt)
                .responseCode(pgResponse.responseCode())
                .approvedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 승인거래 저장 (재수신 시 기존 거래 반환)
     */
    @Override
    @Transactional
    public Payment saveApprovedPayment(PayOrderCommand command, PgApprovalResult approvalResult) {
        Optional<Payment> savedPayment = paymentOutPort.findByOrderId(command.getOrderId());
        if (savedPayment.isPresent()) {
            return savedPayment.get();
        }

        Payment paymentEntity = Payment.create(command, approvalResult.pgProvider());
        paymentEntity.approved(approvalResult);
        paymentOutPort.savePayment(paymentEntity);
        return paymentEntity;
    }

    /**
     * 실패거래 저장 : 거래실패 테이블 (재수신 시 중복 저장 안 함)
     */
    @Override
    @Transactional
    public void savePaymentFailure(OrderId orderId, PgApprovalResult approvalResult) {
        if (paymentOutPort.existsPaymentFailureByOrderId(orderId)) {
            return;
        }
        paymentOutPort.savePaymentFailure(PaymentFailure.create(orderId, approvalResult));
        log.info("Payment failed for orderId: {}, failType: {}", orderId, approvalResult.status());
    }

    /**
     * 망취소 (승인 전액 취소)
     * 취소 대상을 특정할 수 없거나 PG 취소 실패 시 VOID_FAILED (수동 처리 대상)
     */
    private PgApprovalResult voidApproval(PgStrategy pgStrategy,
                                          PayOrderCommand command,
                                          String approvedPgTid,
                                          Long pgApprovedAmt,
                                          PgApprovalStatus voidedStatus,
                                          String reason) {
        PgApprovalResult.PgApprovalResultBuilder result = PgApprovalResult.builder()
                .pgProvider(pgStrategy.getPgProvider())
                .pgTid(approvedPgTid)
                .pgApprovedAmt(pgApprovedAmt);

        String voidTid = null;
        try {
            voidTid = pgStrategy.getVoidTid(command, approvedPgTid);
            if (voidTid == null) {
                log.error("망취소 불가 (취소 대상 거래키 없음) - orderId: {}", command.getOrderId());
                return result.status(PgApprovalStatus.VOID_FAILED)
                        .reason(reason + " / 망취소 불가: 거래키 없음")
                        .build();
            }

            PgPayCancelResponse cancelResponse = pgStrategy.processCancel(PayCancelCommand.builder()
                    .orderId(command.getOrderId())
                    .paymentStatus(PaymentStatus.FULL_CANCELED)
                    .cancelReason(reason)
                    .pgTid(voidTid)
                    .canceledAmount(pgApprovedAmt != null ? Money.of(pgApprovedAmt) : command.getApprovedAmount())
                    .payMethod(command.getPayMethod())
                    .payProvider(command.getPayProvider())
                    .pgProvider(pgStrategy.getPgProvider())
                    .build());

            if (cancelResponse == null || !cancelResponse.isSuccess()) {
                log.error("망취소 실패 - orderId: {}", command.getOrderId());
                return result.status(PgApprovalStatus.VOID_FAILED)
                        .pgTid(voidTid)
                        .responseCode(cancelResponse == null ? null : cancelResponse.responseCode())
                        .reason(reason + " / 망취소 실패")
                        .build();
            }

            return result.status(voidedStatus)
                    .pgTid(voidTid)
                    .pgCancelTid(cancelResponse.pgCcTid())
                    .responseCode(cancelResponse.responseCode())
                    .reason(reason)
                    .canceledAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("망취소 실패 - orderId: {}", command.getOrderId(), e);
            return result.status(PgApprovalStatus.VOID_FAILED)
                    .pgTid(voidTid != null ? voidTid : approvedPgTid)
                    .reason(reason + " / 망취소 실패: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 전체/부분 취소
     */
    @Override
    @Transactional
    public void doCancel(PayCancelCommand cancelCommand) {
        // 결제 검증
        Payment paymentEntity = paymentOutPort.findByOrderId(cancelCommand.getOrderId())
                .orElseThrow(() -> new BusinessException(INVALID_PAYMENT));
        paymentEntity.validateForCancel();

        if(cancelCommand.getPaymentStatus().equals(PaymentStatus.FULL_CANCELED)) {
            validTotalCancel(paymentEntity);
            PgPayCancelResponse pgResponse = processPgCancel(cancelCommand, paymentEntity);
            // 취소상태변경
            paymentEntity.canceled(pgResponse);

        } else if(cancelCommand.getPaymentStatus().equals(PaymentStatus.PARTIAL_CANCELED)) {
            Money remainAmt = validPartCancel(paymentEntity, cancelCommand);
            PgPayCancelResponse pgResponse = processPgCancel(cancelCommand, paymentEntity);
            // 부분취소 저장
            afterPartCancel(cancelCommand, paymentEntity, remainAmt, pgResponse);
        }
    }

    /**
     * pg 취소 진행
     */
    private PgPayCancelResponse processPgCancel(PayCancelCommand cancelCommand, Payment paymentEntity) {
        // 원거래기반 결제 정보 세팅
        cancelCommand.setPgTid(paymentEntity.getPgTid());
        cancelCommand.setPayProvider(paymentEntity.getPayProvider());
        cancelCommand.setPayMethod(paymentEntity.getPayMethod());
        cancelCommand.setPgProvider(paymentEntity.getPgProvider());

        // pg 취소 진행
        PgStrategy pgStrategy = pgRouter.getPgStrategyByProvider(paymentEntity.getPgProvider(), paymentEntity.getPayMethod());
        PgPayCancelResponse pgResponse = pgStrategy.processCancel(cancelCommand);

        // PG 응답 반영
        if (!pgResponse.isSuccess()) {
            throw new BusinessException(PG_RESPONSE_FAILED);
        }

        // Pg취소금액, 요청취소금액 검증
        validRequestAmount(cancelCommand.getCanceledAmount(), pgResponse.cancelAmount());

        return pgResponse;
    }

    /**
     * 전취소 가능여부 확인
     */
    private void validTotalCancel(Payment paymentEntity) {
        // 부분취소 존재여부 확인
        boolean hasPartialCancel = paymentOutPort.existsPartCancelByPaymentId(paymentEntity.getPaymentId());
        if (hasPartialCancel) {
            throw new BusinessException(BusinessError.PAYMENT_HAS_PARTIAL_CANCEL);
        }
    }

    /**
     * 부분취소 가능여부 확인
     */
    private Money validPartCancel(Payment paymentEntity, PayCancelCommand cancelCommand) {
        // 부분취소 내역 조회
        Money remainAmt = null;
        boolean hasPartialCancel = paymentOutPort.existsPartCancelByPaymentId(paymentEntity.getPaymentId());
        if (hasPartialCancel) {
            remainAmt = paymentOutPort.getRemainAmount(paymentEntity.getPaymentId());
        } else {
            remainAmt = paymentEntity.getApprovedAmt();
        }

        // 취소가능금액 검증
        if(remainAmt.value() == 0
                || cancelCommand.getCanceledAmount().isGreaterThan(remainAmt) ) {
            throw new BusinessException(PAYMENT_CANCEL_AMOUNT_EXCEEDED);
        }

        return remainAmt;
    }

    /**
     * 부분취소 후처리
     */
    private Long afterPartCancel(PayCancelCommand cancelCommand,
                                 Payment paymentEntity,
                                 Money remainAmt,
                                 PgPayCancelResponse pgResponse) {
        // 최종 남은금액
        Money refreshRemainAmt = remainAmt.subtract(cancelCommand.getCanceledAmount());
        // 부분취소 내역 저장
        PaymentPartCancel partCancel = PaymentPartCancel.create(
                paymentEntity.getPaymentId(),
                cancelCommand.getCanceledAmount(),
                refreshRemainAmt
        );
        partCancel.completed(pgResponse.pgCcTid());
        paymentOutPort.savePartCancel(partCancel);

        return partCancel.getId();
    }

    /**
     * pg 요청금액 , 고객 요청금액 동일 검증
     * @param customerRequestAmt
     * @param pgResponseAmt
     */
    private void validRequestAmount(Money customerRequestAmt, Long pgResponseAmt) {
        if(customerRequestAmt.value() != pgResponseAmt) {
            throw new RuntimeException("요청금액 불일치");
        }
    }
}
