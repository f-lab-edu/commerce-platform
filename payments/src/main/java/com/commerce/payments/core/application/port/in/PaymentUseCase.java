package com.commerce.payments.core.application.port.in;


import com.commerce.payments.core.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;

public interface PaymentUseCase {
    void doApproval(PayOrderCommand command);
    void doCancel(PayCancelCommand cancelCommand);
}
