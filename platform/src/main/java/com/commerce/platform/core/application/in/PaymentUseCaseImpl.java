package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.CustomerCardOutPort;
import com.commerce.platform.core.application.out.OrderOutputPort;
import com.commerce.platform.core.application.out.PaymentOutPort;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.aggreate.CustomerCard;
import com.commerce.platform.core.domain.aggreate.Order;
import com.commerce.platform.core.domain.aggreate.Payment;
import com.commerce.platform.core.domain.aggreate.PaymentPartCancel;
import com.commerce.platform.core.domain.service.PaymentPgRouter;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.shared.exception.BusinessError;
import com.commerce.platform.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.commerce.platform.shared.exception.BusinessError.*;

@Log4j2
@RequiredArgsConstructor
@Service
public class PaymentUseCaseImpl implements PaymentUseCase{
    private final PaymentPgRouter pgRouter;
    private final PaymentOutPort paymentOutPort;
    private final OrderOutputPort orderOutputPort;
    private final CustomerCardOutPort customerCardOutPort;

    @Override
    @Transactional
    public void doApproval(PayOrderCommand payOrdercommand) {
            // 주문 결제처리
        Order orderEntity = orderOutputPort.findById(payOrdercommand.getOrderId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        orderEntity.validForPay();

        // pg사 라우팅
        PgStrategy pgStrategy = pgRouter.routPg(payOrdercommand.getPayMethod());

        // 결재 entity 생성
        payOrdercommand.setApprovedAmount(orderEntity.getResultAmt());
        Payment paymentEntity = Payment.create(payOrdercommand, pgStrategy.getPgProvider());

        // pg 결제 응답 수신
        PgPayResponse pgResponse = pgStrategy.processApproval(payOrdercommand);

        // 결제 결과에 따른 주문/결제 상태 변경
        orderEntity.changeStatusAfterPay(pgResponse);
        paymentEntity.approved(pgResponse);

        paymentOutPort.savePayment(paymentEntity);

        // todo pg사 응답데이터/ 시간정보 저장?
    }

    /**
     * 전체취소
     */
    @Override
    @Transactional
    public void doCancel(PayOrderCommand payOrderCommand) {

        // 주문 검증
        Order orderEntity = orderOutputPort.findById(payOrderCommand.getOrderId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        orderEntity.validateForCancel();

        // 결제 검증
        Payment paymentEntity = paymentOutPort.findByOrderId(orderEntity.getOrderId())
                .orElseThrow(() -> new BusinessException(INVALID_PAYMENT));
        paymentEntity.validateForCancel();

        // 부분취소 존재여부 확인
        boolean hasPartialCancel = paymentOutPort.existsPartCancelByPaymentId(paymentEntity.getPaymentId());
        if (hasPartialCancel) {
            throw new BusinessException(BusinessError.PAYMENT_HAS_PARTIAL_CANCEL);
        }

        // 취소요청
        PgStrategy pgStrategy = pgRouter.getPgStrategyByProvider(paymentEntity.getPgProvider());
        payOrderCommand.setCancelAmount(paymentEntity.getApprovedAmt());
        PgPayResponse pgResponse = pgStrategy.processCancel(payOrderCommand);

        // PG 응답 반영
        if (!pgResponse.isSuccess()) {
            throw new BusinessException(PG_RESPONSE_FAILED);
        }

        orderEntity.refund();
        paymentEntity.canceled(pgResponse);
        paymentOutPort.savePayment(paymentEntity);

    }

    /**
     * 부분취소
     */
    @Override
    @Transactional
    public void doPartCancel(PayOrderCommand payOrderCommand) {
        // 주문 검정
        Order orderEntity = orderOutputPort.findById(payOrderCommand.getOrderId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
        orderEntity.validateForCancel();

        // 결제 검증
        Payment paymentEntity = paymentOutPort.findByOrderId(orderEntity.getOrderId())
                .orElseThrow(() -> new BusinessException(INVALID_PAYMENT));
        paymentEntity.validateForCancel();

        // 부분취소 내역 조회
        Money remainAmt = null;
        boolean hasPartialCancel = paymentOutPort.existsPartCancelByPaymentId(paymentEntity.getPaymentId());
        if (hasPartialCancel) {
            remainAmt = paymentOutPort.getRemainAmount(paymentEntity.getPaymentId());
        } else {
            remainAmt = paymentEntity.getApprovedAmt();
        }

        // 취소 요청 금액 검증
        Money requestCancelAmt = payOrderCommand.getCancelAmount();
        if (requestCancelAmt.value() > remainAmt.value()) {
            throw new BusinessException(BusinessError.PAYMENT_CANCEL_AMOUNT_EXCEEDED);
        } else if (!hasPartialCancel && requestCancelAmt.value() == remainAmt.value()) {
            throw new BusinessException(INVALID_PARTIAL_CANCEL_AMOUNT);
        }

        // 최종 남은금액
        Money new_remainAmt = remainAmt.subtract(requestCancelAmt);

        // 취소 요청
        PgStrategy pgStrategy = pgRouter.routPg(paymentEntity.getPayMethod());
        payOrderCommand.setCancelAmount(new_remainAmt);
        PgPayResponse pgResponse = pgStrategy.processCancel(payOrderCommand);

        if (!pgResponse.isSuccess()) {
            throw new BusinessException(PG_RESPONSE_FAILED);
        }

        // 부분취소 내역 저장
        PaymentPartCancel partCancel = PaymentPartCancel.create(
                paymentEntity.getPaymentId(),  // Payment 엔티티 전달
                requestCancelAmt,
                new_remainAmt
        );
        partCancel.completed(pgResponse.pgTid());
        paymentOutPort.savePartCancel(partCancel);

    }

    /**
     * 등록된 카드로 결제
     * @param cardId
     */
    @Override
    public void doApprovalWithCardId(Long cardId) {
        CustomerCard customerCard = customerCardOutPort.findActiveById(cardId)
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));

    }

    /**
     * 실패거래건 저장
     */
    private void saveFailedPayment(Payment payment, PgPayResponse pgResponse) {

    }

}
