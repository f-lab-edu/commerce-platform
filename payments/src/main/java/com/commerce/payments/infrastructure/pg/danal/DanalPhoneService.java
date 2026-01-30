package com.commerce.payments.infrastructure.pg.danal;

import com.commerce.payments.core.domain.enums.PayMethod;
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
