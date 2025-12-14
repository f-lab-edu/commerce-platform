package com.commerce.payments.application.port.in.command;

/**
 * 결제 응답
 */
public interface PayResult {

    record Success(
            String paymentId,
            String message
    ) implements PayResult{}

    record Failed(
            String errorCode,
            String message
    ) implements PayResult{}

}
