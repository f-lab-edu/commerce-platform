package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.dto.PgPayCancelResponse;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PgProvider;

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
