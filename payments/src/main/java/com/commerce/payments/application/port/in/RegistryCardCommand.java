package com.commerce.payments.application.port.in;

import com.commerce.payments.domain.enums.PayProvider;
import com.commerce.shared.vo.CustomerId;

public record RegistryCardCommand(
        CustomerId customerId,
        PayProvider payProvider,
        String cardNumber,
        String password,
        String expiryMonth,
        String expiryYear,
        String birthDate,
        String cardNickName
) {
}
