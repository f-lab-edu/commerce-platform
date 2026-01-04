package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.OrderId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.commerce.shared.exception.BusinessError.INVALID_CANCELED_QUANTITY;

@Getter
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "order_id", nullable = false, length = 21))
    OrderId orderId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "product_id", nullable = false, length = 21))
    ProductId productId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity", nullable = false))
    private Quantity quantity;

    private boolean canceled;

    public static OrderItem create(
            OrderId orderId,
            ProductId productId,
            Quantity quantity
    ) {
        OrderItem oi = new OrderItem();
        oi.orderId = orderId;
        oi.productId = productId;
        oi.quantity = quantity;
        oi.canceled = false;
        return oi;
    }

    /**
     * 부분취소 시 해당건 canceld true.
     * 수정된 행은 새로 추가된다.
     */
    public void canceledItem(Quantity canceledQuantity) {
        if(this.quantity.value() < canceledQuantity.value()) {
            throw new BusinessException(INVALID_CANCELED_QUANTITY);
        }
        this.canceled = true;
    }
}
