package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.in.dto.PayResult;
import com.commerce.platform.core.application.out.PaymentOutPort;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.domain.service.PaymentPgRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@Service
public class PaymentUseCaseImpl implements PaymentUseCase{
    private final PaymentPgRouter pgRouter;
    private final PaymentOutPort paymentOutPort;

    @Override
    @Transactional
    public PayResult doApproval(PayOrderCommand command) {
        try {
            PgStrategy pgStrategy = pgRouter.routPg(command.payMethod());

            // todo 임시로 pg사 응답 타입 map
            Map<String, String> pgResponse = pgStrategy.processApproval();

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

        } catch (Exception e) {
            log.error(e.getMessage());
            return new PayResult.Failed("errorCode", "errorMessage");
        }
        // update payment
        return new PayResult.Success(null, null);
    }

    @Override
    @Transactional
    public PayResult doPartCancel(PayOrderCommand command) {
        try {
            // insert payment_part_cancel
            return new PayResult.Success(null, null);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new PayResult.Failed("errorCode", "errorMessage");
        }
    }

}
