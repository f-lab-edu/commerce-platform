package com.commerce.payments.application.port.in;


import com.commerce.payments.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.application.port.in.dto.PayOrderCommand;

public interface PaymentUseCase {
    void doApproval(PayOrderCommand command);
    void doCancel(PayCancelCommand cancelCommand);
}
