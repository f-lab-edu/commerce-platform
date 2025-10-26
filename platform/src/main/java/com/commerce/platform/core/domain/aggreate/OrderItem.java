package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.core.domain.vo.Quantity;
import lombok.Getter;

@Getter
public class OrderItem {
    private ProductId productId;
    private String productName;
    private Money amt;
    private Quantity quantity;

    public static OrderItem create(
            ProductId productId,
            Money amt,
            Quantity quantity
    ) {
       OrderItem orderItem = new OrderItem();
       orderItem.productId = productId;
       orderItem.amt = amt;
       orderItem.quantity = quantity;

       return orderItem;
    }

    public Money calculateOrderItem() {
        return this.amt.multiply(this.quantity);
    }

    /**
     * 상품정보 세팅
     */
    public void setProductInfo(String productName, Money amt) {
        this.productName = productName;
        this.amt = amt;
    }
}
