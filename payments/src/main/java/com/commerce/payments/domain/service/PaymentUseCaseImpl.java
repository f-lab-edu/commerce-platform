package com.commerce.payments.domain.service;

import com.commerce.payments.domain.enums.PaymentStatus;
import com.commerce.payments.domain.vo.payments.PgPayCancelResponse;
import com.commerce.payments.domain.vo.payments.PgPayResponse;
import com.commerce.payments.application.port.in.PaymentUseCase;
import com.commerce.payments.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.application.port.out.PaymentOutPort;
import com.commerce.payments.application.port.out.PgStrategy;
import com.commerce.payments.domain.aggregate.Payment;
import com.commerce.payments.domain.aggregate.PaymentPartCancel;
import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // pg사 라우팅
        PgStrategy pgStrategy = pgRouter.routePg(payOrdercommand.getPayMethod(), payOrdercommand.getPayProvider());

        // 결재 entity 생성
        Payment paymentEntity = Payment.create(payOrdercommand, pgStrategy.getPgProvider());

        // pg 결제 응답 수신
        PgPayResponse pgResponse = pgStrategy.processApproval(payOrdercommand);
        validRequestAmount(payOrdercommand.getApprovedAmount(), pgResponse.amount().value());

        // 승인거래 저장
        paymentEntity.approved(pgResponse);
        paymentOutPort.savePayment(paymentEntity);
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
            Money remainAmt = validPartCancel(paymentEntity);
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
    private Money validPartCancel(Payment paymentEntity) {
        // 부분취소 내역 조회
        Money remainAmt = null;
        boolean hasPartialCancel = paymentOutPort.existsPartCancelByPaymentId(paymentEntity.getPaymentId());
        if (hasPartialCancel) {
            remainAmt = paymentOutPort.getRemainAmount(paymentEntity.getPaymentId());
        } else {
            remainAmt = paymentEntity.getApprovedAmt();
        }

        // 취소가능금액 검증
        if(remainAmt.value() == 0) {
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
