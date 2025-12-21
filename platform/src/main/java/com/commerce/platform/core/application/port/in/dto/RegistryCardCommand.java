package com.commerce.platform.core.application.port.in.dto;

import com.commerce.shared.enums.PayProvider;
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
