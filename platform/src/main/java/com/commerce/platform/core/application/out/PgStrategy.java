package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PgProvider;

public abstract class PgStrategy {

    public abstract PgProvider getPgProvider();

    // tmp
    public PgPayResponse processApproval(PayOrderCommand payOrdercommand) {
        return null;
    }

    public PgPayResponse processCancel(PayCancelCommand cancelCommand) {
        return null;
    }
}
