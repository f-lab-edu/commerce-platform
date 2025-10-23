package com.commerce.platform.core.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    CANCELED("취소"),
    REFUND("환불"),
    PENDING("주문대기"),
    CONFIRMED("주문완료"),
    PAID("결제완료"),
    PREPARING("상품준비중"),
    READY_TO_SHIP("배송준비중"),
    SHIPPING("배송중"),
    DELIVERED("배송완료");

    private final String value;
}
