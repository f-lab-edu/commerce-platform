package com.commerce.payments.core.application.port.in;


import com.commerce.payments.core.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.domain.aggregate.Payment;
import com.commerce.payments.core.domain.vo.payments.PgApprovalResult;
import com.commerce.shared.vo.OrderId;

public interface PaymentUseCase {
    void doApproval(PayOrderCommand command);
    void doCancel(PayCancelCommand cancelCommand);

    /**
     * PG 승인 (+ 금액 불일치/결과 불명 시 망취소). DB 저장 없음
     */
    PgApprovalResult approveWithPg(PayOrderCommand command);

    /**
     * 승인 거래 저장
     */
    Payment saveApprovedPayment(PayOrderCommand command, PgApprovalResult approvalResult);

    /**
     * 실패 거래 저장
     */
    void savePaymentFailure(OrderId orderId, PgApprovalResult approvalResult);
}
