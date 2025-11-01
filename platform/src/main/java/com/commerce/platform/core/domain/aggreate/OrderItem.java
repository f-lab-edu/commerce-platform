package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.OrderId;
import com.commerce.platform.core.domain.vo.OrderItemId;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.core.domain.vo.Quantity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @EmbeddedId
    private OrderItemId orderItemId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity", nullable = false))
    private Quantity quantity;

    public static OrderItem create(
            OrderId orderId,
            ProductId productId,
            Quantity quantity
    ) {
       return OrderItem.builder()
               .orderItemId(new OrderItemId(orderId, productId))
               .quantity(quantity)
               .build();
    }

    @Builder
    private OrderItem(OrderItemId orderItemId, Quantity quantity) {
        this.orderItemId = orderItemId;
        this.quantity = quantity;
    }
}
