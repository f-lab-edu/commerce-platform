package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.in.dto.PayResult;
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
import com.commerce.platform.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_ORDER_ID;
import static com.commerce.platform.shared.exception.BusinessError.INVALID_PAYMENT;

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
    public PayResult doApproval(PayOrderCommand payOrdercommand) {
        try {

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

            return new PayResult.Success(paymentEntity.getPaymentId().id(), "성공");
        } catch (Exception e) {
            log.error(e.getMessage());
            return new PayResult.Failed("errorCode", "errorMessage");
        }
    }

    @Override
    @Transactional
    public PayResult doCancel(PayOrderCommand payOrdercommand) {
        try {
            // 주문 조회
            Order orderEntity = orderOutputPort.findById(payOrdercommand.getOrderId())
                    .orElseThrow(() -> new BusinessException(INVALID_ORDER_ID));

            Payment paymentEntity =  paymentOutPort.findByOrderId(orderEntity.getOrderId())
                    .orElseThrow(() -> new BusinessException(INVALID_PAYMENT));

            // todo refund check valid
            orderEntity.refund();
            // todo payment valid refund check


            // todo 결제했던 pg 가져오기
            PgStrategy pgStrategy = null;
            // todo 승인금액 이랑 비교해서 전체/부분취소 세팅한다.
            PgPayResponse pgResponse = pgStrategy.processCancel(payOrdercommand);

            // todo 전체취소면 update payment
            // todo 부분취소면 insert payment_part_cancel
        } catch (Exception e) {
            log.error(e.getMessage());
            return new PayResult.Failed("errorCode", "errorMessage");
        }
        return new PayResult.Success(null, null);
    }

    @Override
    public PayResult doPartCancel(PayOrderCommand cancel) {
        // 부분취소 이력 저장
        PaymentPartCancel partCancelEntity = null;

        return null;
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
