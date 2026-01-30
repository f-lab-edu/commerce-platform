package com.commerce.payments.infrastructure.pg.danal;


import com.commerce.payments.core.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.application.port.out.PgStrategy;
import com.commerce.payments.core.domain.vo.payments.PgPayCancelResponse;
import com.commerce.payments.core.domain.vo.payments.PgPayResponse;
import com.commerce.payments.core.domain.enums.PayMethod;
import com.commerce.payments.core.domain.enums.PgProvider;

/**
 * 여기서 필요에 따라 다날의 결제수단별 프로세스를 추상화한다.
 */
public abstract class DanalStrategy extends PgStrategy {

    @Override
    public PgPayResponse processApproval(PayOrderCommand command) {
        return null;
    }

    @Override
    public PgPayCancelResponse processCancel(PayCancelCommand command) {
        return null;
    }


    @Override
    public PgProvider getPgProvider() {
        return PgProvider.DANAL;
    }

    @Override
    public PayMethod getPgPayMethod() {
        return getDanalPayMethod();
    }

    /**
     * Danal 구현체 중 특정 결제서비스 빈 추출을 위함
     * @return
     */
    protected abstract PayMethod getDanalPayMethod();

}
