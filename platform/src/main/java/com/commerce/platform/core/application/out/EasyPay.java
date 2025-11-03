package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;

import java.util.Map;

public interface EasyPay {
    Map<String, String> approveEasyPay(PayOrderCommand command);
    Map<String, String> cancelEasyPay(PayOrderCommand command);
}
