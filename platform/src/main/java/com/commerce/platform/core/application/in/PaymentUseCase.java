package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.in.dto.PayResult;

public interface PaymentUseCase {
    PayResult doApproval(PayOrderCommand command);
    PayResult doCancel(PayOrderCommand command);
    PayResult doPartCancel(PayOrderCommand cancel);
    void doApprovalWithCardId(Long cardId);   // 등록된 카드로 결제
}
