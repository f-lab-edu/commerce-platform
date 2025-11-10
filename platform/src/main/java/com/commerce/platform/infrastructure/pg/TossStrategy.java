package com.commerce.platform.infrastructure.pg;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.CardPay;
import com.commerce.platform.core.application.out.EasyPay;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PgProvider;
import org.springframework.stereotype.Component;

@Component
public class TossStrategy extends PgStrategy
        implements CardPay, EasyPay {

    @Override
    public PgProvider getPgProvider() {
        return PgProvider.TOSS;
    }


    @Override
    public PgPayResponse approveCard(PayOrderCommand command) {
        return null;
    }

    @Override
    public PgPayResponse cancelCard(PayCancelCommand command) {
        return null;
    }

    @Override
    public PgPayResponse approveEasyPay(PayOrderCommand command) {
        return null;
    }

    @Override
    public PgPayResponse cancelEasyPay(PayCancelCommand command) {
        return null;
    }
}
