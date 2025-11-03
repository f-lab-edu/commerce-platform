package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.in.dto.PayResult;

public interface PaymentUseCase {
    PayResult doApproval(PayOrderCommand command);
    PayResult doCancel(PayOrderCommand command);
    PayResult doPartCancel(PayOrderCommand command);
}
