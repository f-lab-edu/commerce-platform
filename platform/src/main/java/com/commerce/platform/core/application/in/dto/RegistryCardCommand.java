package com.commerce.platform.core.application.in.dto;

import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.vo.CustomerId;

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
