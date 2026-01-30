package com.commerce.payments.core.application.port.out;


import com.commerce.payments.core.domain.vo.payments.PgPayCancelResponse;
import com.commerce.payments.core.domain.vo.payments.PgPayResponse;
import com.commerce.payments.core.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.domain.enums.PayMethod;
import com.commerce.payments.core.domain.enums.PgProvider;

/**
 * PG사별 결제를 위한 메소드 정의
 */
public abstract class PgStrategy {

    /**
     * 승인
     */
    public abstract PgPayResponse processApproval(PayOrderCommand command);

    /**
     * 취소
     */
    public abstract PgPayCancelResponse processCancel(PayCancelCommand command);

    /**
     * PG사명
     */
    public abstract PgProvider getPgProvider();

    /**
     * 결제유형
     */
    public abstract PayMethod getPgPayMethod();

    /**
     * 결제창을 위한 초기화
     */
    public abstract Object initPayment();

}
