package com.commerce.platform.infrastructure.pg;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.PhonePay;
import com.commerce.platform.core.domain.enums.PgProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DanalStrategy extends PgStrategy
        implements PhonePay {
    @Override
    public PgProvider getPgProvider() {
        return PgProvider.DANAL;
    }

    @Override
    public Map<String, String> approvePhone(PayOrderCommand command) {
        return null;
    }

    @Override
    public Map<String, String> cancelPhone(PayOrderCommand command) {
        return null;
    }
}
