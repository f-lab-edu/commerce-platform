package com.commerce.order.bootstrap.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderRequest(
        @NotBlank(message = "고객 ID는 필수입니다")
        String customerId,
        String couponId,
        @NotEmpty(message = "주문 상품은 1개 이상이어야 합니다")
        @Valid
        List<OrderItemRequest> orderItemRequests,
        @NotBlank(message = "결제 수단은 필수입니다")
        String payMethod,
        @NotBlank(message = "결제사는 필수입니다")
        String payProvider
){
    public record OrderItemRequest(
            @NotBlank(message = "상품 ID는 필수입니다")
            String productId,
            @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
            int quantity
    ) {}
}
