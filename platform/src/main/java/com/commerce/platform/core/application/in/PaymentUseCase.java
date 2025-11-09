package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;

public interface PaymentUseCase {
    void doApproval(PayOrderCommand command);
    void doCancel(PayOrderCommand command);
    void doPartCancel(PayOrderCommand cancel);
    void doApprovalWithCardId(Long cardId);   // 등록된 카드로 결제
}
