package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;

public interface PaymentUseCase {
    void doApproval(PayOrderCommand command);
    void doCancel(PayCancelCommand cancelCommand);
    Long doPartCancel(PayCancelCommand cancelCommand);
    void doApprovalWithCardId(Long cardId);   // 등록된 카드로 결제
}
