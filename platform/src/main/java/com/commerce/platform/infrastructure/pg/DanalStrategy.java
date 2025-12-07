package com.commerce.platform.infrastructure.pg;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayCancelResponse;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PgProvider;
import org.springframework.stereotype.Component;

@Component
public class DanalStrategy extends PgStrategy {

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

}
