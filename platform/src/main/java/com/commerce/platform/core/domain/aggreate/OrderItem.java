package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.core.domain.vo.Quantity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class OrderItem {
    private ProductId productId;
    private String productName;
    private Money amt;
    private Quantity quantity;

    public static OrderItem create(
            ProductId productId,
            String productName,
            Money amt,
            Quantity quantity
    ) {
       return OrderItem.builder()
               .productId(productId)
               .productName(productName)
               .amt(amt)
               .quantity(quantity)
               .build();
    }

    public Money calculateOrderItem() {
        return this.amt.multiply(this.quantity);
    }

}
