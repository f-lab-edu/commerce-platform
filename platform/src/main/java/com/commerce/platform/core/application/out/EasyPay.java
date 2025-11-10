package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.dto.PgPayResponse;

public interface EasyPay {
    PgPayResponse approveEasyPay(PayOrderCommand command);
    PgPayResponse cancelEasyPay(PayCancelCommand command);
}
