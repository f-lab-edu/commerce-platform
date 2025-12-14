package com.commerce.payments.application.port.out;

import com.commerce.payments.application.port.in.command.PayApprovalCommand;
import com.commerce.payments.application.port.out.dto.PgApprovalResponse;
import com.commerce.payments.application.port.out.dto.PgCancelResponse;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.PaymentId;

/**
 * PG사 통신 Port (Outbound Port)
 */
public interface PgPort {
    
    /**
     * PG사 승인 요청
     */
    PgApprovalResponse requestApproval(PayApprovalCommand command);
    
    /**
     * PG사 취소 요청
     */
    PgCancelResponse requestCancel(PaymentId paymentId, Money cancelAmount);
}
