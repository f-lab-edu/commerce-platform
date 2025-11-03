package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.in.dto.PayResult;
import com.commerce.platform.core.application.out.CustomerCardOutPort;
import com.commerce.platform.core.application.out.PaymentOutPort;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.domain.aggreate.CustomerCard;
import com.commerce.platform.core.domain.service.PaymentPgRouter;
import com.commerce.platform.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_ORDER_ID;

@Log4j2
@RequiredArgsConstructor
@Service
public class PaymentUseCaseImpl implements PaymentUseCase{
    private final PaymentPgRouter pgRouter;
    private final PaymentOutPort paymentOutPort;
    private final CustomerCardOutPort customerCardOutPort;

    @Override
    @Transactional
    public PayResult doApproval(PayOrderCommand command) {
        try {
            PgStrategy pgStrategy = pgRouter.routPg(command.payMethod());

            // todo 임시로 pg사 응답 타입 map
            Map<String, String> pgResponse = pgStrategy.processApproval(command);

            // todo payment entity 생성

            return new PayResult.Success(null, null);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new PayResult.Failed("errorCode", "errorMessage");
        }
    }

    @Override
    @Transactional
    public PayResult doCancel(PayOrderCommand command) {
        try {
            PgStrategy pgStrategy = pgRouter.routPg(command.payMethod());

            // todo 승인금액 이랑 비교해서 전체/부분취소 세팅한다.
            Map<String, String> pgResponse = pgStrategy.processCancel(command);

            // todo 전체취소면 update payment
            // todo 부분취소면 insert payment_part_cancel
        } catch (Exception e) {
            log.error(e.getMessage());
            return new PayResult.Failed("errorCode", "errorMessage");
        }
        return new PayResult.Success(null, null);
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
