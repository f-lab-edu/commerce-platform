package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;

import java.util.Map;

public interface PhonePay {
    Map<String, String> approvePhone(PayOrderCommand command);
    Map<String, String> cancelPhone(PayOrderCommand command);
}
