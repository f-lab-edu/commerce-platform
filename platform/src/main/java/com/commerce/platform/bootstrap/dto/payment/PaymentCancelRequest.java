package com.commerce.platform.bootstrap.dto.payment;

import com.commerce.platform.core.domain.enums.PaymentStatus;
import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.Quantity;
import jakarta.validation.constraints.NotBlank;

/**
 * 전채/부분 취소 요청 dto
 */
public record PaymentCancelRequest(
        @NotBlank
        OrderId orderId,

        Long orderItemId,   // 취소할 orderItem

        Quantity canceledQuantity, // 해당 orderItem의 취소 개수

        @NotBlank
        PaymentStatus paymentStatus
) {}