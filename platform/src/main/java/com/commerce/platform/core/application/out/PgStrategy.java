package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.dto.PgPayCancelResponse;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PgProvider;

public abstract class PgStrategy {

    /**
     * pg사별 요청에 따라 [Card | Easy | Phone]PayService 구현체 실행한다.
     *
     * @param command
     * @return todo 결재응답dto
     */
    public abstract PgPayResponse processApproval(PayOrderCommand command);

    public abstract PgPayCancelResponse processCancel(PayCancelCommand command);

    public abstract PgProvider getPgProvider();

    public abstract PayMethod getPgPayMethod();

}
