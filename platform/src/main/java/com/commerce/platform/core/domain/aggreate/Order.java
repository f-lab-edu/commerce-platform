package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class Order {
    private String orderId;
    private String customerId;
    private String couponId;
    private List<OrderItem> orderItems;
    private Money originAmt;     // 할인전금액
    private Money discountAmt;   // 할인금액
    private Money resultAmt;     // 최종금액
    private OrderStatus status;
    private LocalDateTime orderedDateTime;

    public static Order create(
            String customerId,
            List<OrderItem> orderItems
    ) throws Exception {
        // 주문내역 확인
        for (OrderItem orderItem : orderItems) {
            orderItem.checkOrderItem();
        }

        Order order = new Order();
        order.orderId = String.valueOf(UUID.randomUUID());
        order.customerId = customerId;
        order.orderItems = orderItems;
        order.discountAmt = Money.create(0);
        order.status = OrderStatus.PENDING;
        order.orderedDateTime = LocalDateTime.now();

        // 원금액 계산
        order.calculateAmt();

        return order;
    }

    private void calculateAmt() throws Exception {
        this.originAmt = orderItems.stream()
                .map(OrderItem::calculateOrderItem)
                .reduce(Money.create(0L), Money::add);

        this.resultAmt = this.originAmt.substract(this.discountAmt);
    }

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

    // todo 주문상태 변경
    public void changeStatus(OrderStatus status) {
        this.status = status;
    }

}
