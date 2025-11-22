package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.*;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.aggreate.*;
import com.commerce.platform.core.domain.service.PaymentPgRouter;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.shared.exception.BusinessError;
import com.commerce.platform.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.commerce.platform.shared.exception.BusinessError.*;

@Log4j2
@RequiredArgsConstructor
@Service
public class PaymentUseCaseImpl implements PaymentUseCase{
    private final PaymentPgRouter pgRouter;
    private final PaymentOutPort paymentOutPort;
    private final OrderOutputPort orderOutputPort;
    private final OrderItemOutPort orderItemOutPort;
    private final ProductOutputPort productOutputPort;
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
    public void doCancel(PayCancelCommand cancelCommand) {

        // 주문 검증
        Order orderEntity = orderOutputPort.findById(cancelCommand.getOrderId())
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

        cancelCommand.setCanceledAmount(orderEntity.getResultAmt());
        cancelCommand.setPayProvider(paymentEntity.getPayProvider());
        cancelCommand.setPayMethod(paymentEntity.getPayMethod());
        cancelCommand.setPgProvider(paymentEntity.getPgProvider());

        PgStrategy pgStrategy = pgRouter.getPgStrategyByProvider(paymentEntity.getPgProvider());
        PgPayResponse pgResponse = pgStrategy.processCancel(cancelCommand);

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
    public Long doPartCancel(PayCancelCommand cancelCommand) {
        // 주문 검정
        Order orderEntity = orderOutputPort.findById(cancelCommand.getOrderId())
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

        // 취소가능금액 검증
        if(remainAmt.value() == 0) {
            throw new BusinessException(PAYMENT_CANCEL_AMOUNT_EXCEEDED);
        }

        // 부분취소 가능수량 검증
        OrderItem orderItemEntity = orderItemOutPort.findById(cancelCommand.getOrderItemId())
                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ITEM_ID));
        // 해당 건 삭제처리
        orderItemEntity.canceledItem(cancelCommand.getCanceledQuantity());
        // 새롭게 행 생성한다.
        OrderItem refreshOrderItem = OrderItem.create(cancelCommand.getOrderId(),
                orderItemEntity.getProductId(),
                orderItemEntity.getQuantity().minus(cancelCommand.getCanceledQuantity()));
        orderItemOutPort.saveAll(List.of(refreshOrderItem));

        // 취소금액 계산
        Money canceledAmt = productOutputPort.findById(orderItemEntity.getProductId())
                .get()
                .getPrice().multiply(cancelCommand.getCanceledQuantity());

        cancelCommand.setCanceledAmount(canceledAmt);
        cancelCommand.setPayProvider(paymentEntity.getPayProvider());
        cancelCommand.setPayMethod(paymentEntity.getPayMethod());
        cancelCommand.setPgProvider(paymentEntity.getPgProvider());

        // 최종 남은금액
        Money refreshRemainAmt = remainAmt.subtract(canceledAmt);

        // 취소 요청
        cancelCommand.setCanceledAmount(paymentEntity.getApprovedAmt());
        cancelCommand.setPayProvider(paymentEntity.getPayProvider());
        cancelCommand.setPayMethod(paymentEntity.getPayMethod());
        cancelCommand.setPgProvider(paymentEntity.getPgProvider());

        PgStrategy pgStrategy = pgRouter.getPgStrategyByProvider(paymentEntity.getPgProvider());
        PgPayResponse pgResponse = pgStrategy.processCancel(cancelCommand);

        if (!pgResponse.isSuccess()) {
            throw new BusinessException(PG_RESPONSE_FAILED);
        }

        // 부분취소 내역 저장
        PaymentPartCancel partCancel = PaymentPartCancel.create(
                paymentEntity.getPaymentId(),
                canceledAmt,
                refreshRemainAmt
        );
        partCancel.completed(pgResponse.pgTid());
        paymentOutPort.savePartCancel(partCancel);

        return partCancel.getId();
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

}
