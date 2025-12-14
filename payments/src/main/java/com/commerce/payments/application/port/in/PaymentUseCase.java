package com.commerce.payments.application.port.in;


import com.commerce.payments.application.port.in.command.PayCancelCommand;
import com.commerce.payments.application.port.in.command.PayOrderCommand;

public interface PaymentUseCase {
    void doApproval(PayOrderCommand command);
    void doCancel(PayCancelCommand cancelCommand);
    Long doPartCancel(PayCancelCommand cancelCommand);
}
