package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.enums.OrderStatus;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class Order {
    private OrderId orderId;
    private CustomerId customerId;
    private CouponId couponId;
    private List<OrderItem> orderItems;
    private Money originAmt;     // 할인전금액
    private Money discountAmt;   // 할인금액
    private Money resultAmt;     // 최종금액
    private OrderStatus status;
    private LocalDateTime orderedDateTime;
    private LocalDateTime updatedDateTime;

    public static Order create(
            CustomerId customerId,
            CouponId couponId,
            List<OrderItem> orderItems
    ) {
        return Order.builder()
                .orderId(OrderId.create())
                .customerId(customerId)
                .couponId(couponId)
                .orderItems(orderItems)
                .discountAmt(Money.create(0))
                .status(OrderStatus.PENDING)
                .orderedDateTime(LocalDateTime.now())
                .build();

        // 원금액 계산
//        order.calculateAmt();
    }

    /**
     * 주문상품list 검증
     */
    public void checkOrderItems() {
//        this.getOrderItems().stream()
//                .forEach(OrderItem::checkOrderItem);
    }

    /** 원금액, 할인금액, 최종금액 계산*/

    public void applyCoupon(String couponId, Money discountAmt) throws Exception {
        if(couponId == null) return;
        else if(this.status != OrderStatus.PENDING) {
            throw new Exception("쿠촌적용 실패");
        }

        // 할인금액
        this.discountAmt = discountAmt;

        // 최종금액
        this.calculateAmt();
    }

    private void calculateAmt() throws Exception {
        this.originAmt = orderItems.stream()
                .map(OrderItem::calculateOrderItem)
                .reduce(Money.create(0L), Money::add);

        this.resultAmt = this.originAmt.subtract(this.discountAmt);
    }

    // todo 주문상태 변경
    public void changeStatus(OrderStatus status) {
        this.status = status;
    }

}
