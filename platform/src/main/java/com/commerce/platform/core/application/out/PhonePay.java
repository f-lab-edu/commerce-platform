package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.dto.PgPayResponse;

public interface PhonePay {
    PgPayResponse approvePhone(PayOrderCommand command);
    PgPayResponse cancelPhone(PayCancelCommand command);
}
