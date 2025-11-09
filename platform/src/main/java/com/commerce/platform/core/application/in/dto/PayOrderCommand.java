package com.commerce.platform.core.application.in.dto;

import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.enums.PaymentStatus;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PayOrderCommand {
        private OrderId orderId;
        private Money approvedAmount;
        private Money cancelAmount;
        private String installment;
        private PayMethod payMethod;
        private PayProvider payProvider;
        private PaymentStatus paymentStatus;
}
