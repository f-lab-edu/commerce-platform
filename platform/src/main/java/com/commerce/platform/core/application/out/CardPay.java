package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.dto.PgPayResponse;

public interface CardPay {
    PgPayResponse approveCard(PayOrderCommand command);
    PgPayResponse cancelCard(PayOrderCommand command);
}
