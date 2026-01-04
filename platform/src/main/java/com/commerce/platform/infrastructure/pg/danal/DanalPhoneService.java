package com.commerce.platform.infrastructure.pg.danal;

import com.commerce.platform.core.domain.enums.PayMethod;
import org.springframework.stereotype.Service;

@Service
public class DanalPhoneService extends DanalStrategy{
    @Override
    protected PayMethod getDanalPayMethod() {
        return PayMethod.PHONE;
    }

    @Override
    public Object initPayment() {
        return null;
    }
}
