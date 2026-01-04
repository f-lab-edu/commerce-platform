package com.commerce.payments.domain.service;

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
//    private final OrderOutputPort orderOutputPort;
//    private final OrderItemOutPort orderItemOutPort;
//    private final ProductOutputPort productOutputPort;
//    private final CustomerCardOutPort customerCardOutPort;

    @Override
    @Transactional
    public void doApproval(PayOrderCommand payOrdercommand) {
            // 주문 결제처리
//        Order orderEntity = orderOutputPort.findById(payOrdercommand.getOrderId())
//                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
//        orderEntity.validForPay();

//        Order orderEntity = null;

        // pg사 라우팅
        PgStrategy pgStrategy = pgRouter.routePg(payOrdercommand.getPayMethod(), payOrdercommand.getPayProvider());

        // 결재 entity 생성
        Payment paymentEntity = Payment.create(payOrdercommand, pgStrategy.getPgProvider());

        // pg 결제 응답 수신
        PgPayResponse pgResponse = pgStrategy.processApproval(payOrdercommand);

        validRequestAmount(payOrdercommand.getApprovedAmount(), pgResponse.amount().value());

        // 결제 결과에 따른 주문/결제 상태 변경
//        orderEntity.changeStatusAfterPay(pgResponse);
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
//        Order orderEntity = orderOutputPort.findById(cancelCommand.getOrderId())
//                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
//        orderEntity.validateForCancel();

        // 결제 검증
        Payment paymentEntity = paymentOutPort.findByOrderId(cancelCommand.getOrderId())
                .orElseThrow(() -> new BusinessException(INVALID_PAYMENT));
        paymentEntity.validateForCancel();

        // 부분취소 존재여부 확인
        boolean hasPartialCancel = paymentOutPort.existsPartCancelByPaymentId(paymentEntity.getPaymentId());
        if (hasPartialCancel) {
            throw new BusinessException(BusinessError.PAYMENT_HAS_PARTIAL_CANCEL);
        }

        cancelCommand.setCanceledAmount(cancelCommand.getCanceledAmount());
        cancelCommand.setPayProvider(paymentEntity.getPayProvider());
        cancelCommand.setPayMethod(paymentEntity.getPayMethod());
        cancelCommand.setPgProvider(paymentEntity.getPgProvider());

        PgStrategy pgStrategy = pgRouter.getPgStrategyByProvider(paymentEntity.getPgProvider(), paymentEntity.getPayMethod());
        PgPayCancelResponse pgResponse = pgStrategy.processCancel(cancelCommand);

        // PG 응답 반영
        if (!pgResponse.isSuccess()) {
            throw new BusinessException(PG_RESPONSE_FAILED);
        }

        // Pg취소금액, 요청취소금액 검증
        validRequestAmount(cancelCommand.getCanceledAmount(), pgResponse.cancelAmount());

//        orderEntity.refund();
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
//        Order orderEntity = orderOutputPort.findById(cancelCommand.getOrderId())
//                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));
//        orderEntity.validateForCancel();

        // 결제 검증
        Payment paymentEntity = paymentOutPort.findByOrderId(cancelCommand.getOrderId())
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
//        OrderItem orderItemEntity = orderItemOutPort.findById(cancelCommand.getOrderItemId())
//                .orElseThrow(() -> new BusinessException(INVALID_ORDER_ITEM_ID));
//        // 해당 건 삭제처리
//        orderItemEntity.canceledItem(cancelCommand.getCanceledQuantity());
//        // 새롭게 행 생성한다.
//        OrderItem refreshOrderItem = OrderItem.create(cancelCommand.getOrderId(),
//                orderItemEntity.getProductId(),
//                orderItemEntity.getQuantity().minus(cancelCommand.getCanceledQuantity()));
//        orderItemOutPort.saveAll(List.of(refreshOrderItem));

        // 취소금액 계산
//        Money canceledAmt = productOutputPort.findById(orderItemEntity.getProductId())
//                .get()
//                .getPrice().multiply(cancelCommand.getCanceledQuantity());

        cancelCommand.setCanceledAmount(cancelCommand.getCanceledAmount());
        cancelCommand.setPayProvider(paymentEntity.getPayProvider());
        cancelCommand.setPayMethod(paymentEntity.getPayMethod());
        cancelCommand.setPgProvider(paymentEntity.getPgProvider());

        // 최종 남은금액
        Money refreshRemainAmt = remainAmt.subtract(cancelCommand.getCanceledAmount());

        // 취소 요청
        cancelCommand.setCanceledAmount(paymentEntity.getApprovedAmt());
        cancelCommand.setPayProvider(paymentEntity.getPayProvider());
        cancelCommand.setPayMethod(paymentEntity.getPayMethod());
        cancelCommand.setPgProvider(paymentEntity.getPgProvider());

        PgStrategy pgStrategy = pgRouter.getPgStrategyByProvider(paymentEntity.getPgProvider(), paymentEntity.getPayMethod());
        PgPayCancelResponse pgResponse = pgStrategy.processCancel(cancelCommand);

        if (!pgResponse.isSuccess()) {
            throw new BusinessException(PG_RESPONSE_FAILED);
        }

        validRequestAmount(cancelCommand.getCanceledAmount(), pgResponse.cancelAmount());

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
