package com.commerce.payments.application.port.out;

import com.commerce.payments.PgPayResponse;
import com.commerce.payments.application.port.in.command.PayCancelCommand;
import com.commerce.payments.application.port.in.command.PayOrderCommand;

public interface PhonePay {
    PgPayResponse approvePhone(PayOrderCommand command);
    PgPayResponse cancelPhone(PayCancelCommand command);
}
