package com.commerce.platform.infrastructure.pg;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.CardPay;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PgProvider;
import org.springframework.stereotype.Component;

@Component
public class NHNStrategy extends PgStrategy
        implements CardPay {
    @Override
    public PgPayResponse approveCard(PayOrderCommand command) {
        return null;
    }

    @Override
    public PgPayResponse cancelCard(PayOrderCommand command) {
        return null;
    }

    @Override
    public PgProvider getPgProvider() {
        return PgProvider.NHN;
    }
}
