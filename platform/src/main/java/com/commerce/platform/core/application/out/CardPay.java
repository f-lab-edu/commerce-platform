package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;

import java.util.Map;

public interface CardPay {
    Map<String, String> approveCard(PayOrderCommand command);
    Map<String, String> cancelCard(PayOrderCommand command);
}
