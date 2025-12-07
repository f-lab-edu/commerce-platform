package com.commerce.platform.infrastructure.pg.danal;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayCancelResponse;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PgProvider;

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
