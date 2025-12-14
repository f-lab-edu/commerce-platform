package com.commerce.payments.application.port.out;


import com.commerce.payments.PgPayResponse;
import com.commerce.payments.application.port.in.command.PayCancelCommand;
import com.commerce.payments.application.port.in.command.PayOrderCommand;

public interface CardPay {
    PgPayResponse approveCard(PayOrderCommand command);
    PgPayResponse cancelCard(PayCancelCommand command);
}
