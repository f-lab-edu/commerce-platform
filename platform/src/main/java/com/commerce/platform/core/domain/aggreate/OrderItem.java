package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.Quantity;

public class OrderItem {
//    private String orderItemId;
    private String productId; // todo 참조의 의미를 위해서 외부 에그리거트는 타입을 만들었던건가?
    private Money amt;
    private Quantity quantity;

    public static OrderItem create(
            String productId,
            long amt,
            int quantity
    ) throws Exception {
       OrderItem orderItem = new OrderItem();
//       orderItem.orderItemId = String.valueOf(UUID.randomUUID());
       orderItem.productId = productId;
       orderItem.amt = Money.create(amt);
       orderItem.quantity = Quantity.create(quantity);

       return orderItem;
    }

    public void checkOrderItem() throws Exception {
       this.quantity.checkQuantity();
    }

    public Money calculateOrderItem() {
        return this.amt.multiply(this.quantity);
    }
}
