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

import static com.commerce.platform.core.domain.enums.OrderStatus.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Order {
    private OrderId orderId;
    private CustomerId customerId;
    private CouponId couponId;
    private List<OrderItem> orderItems;
    private Money originAmt;     // 할인전금액
    private Money discountAmt;   // 할인금액
    private Money resultAmt;     // 최종금액
    private OrderStatus status;
    private LocalDateTime orderedAt;
    private LocalDateTime updatedAt;

    public static Order create(
            CustomerId customerId,
            List<OrderItem> orderItems,
            CouponId couponId
    ) {
        return Order.builder()
                .orderId(OrderId.create())
                .customerId(customerId)
                .couponId(couponId)
                .orderItems(orderItems)
                .discountAmt(Money.create(0))
                .originAmt(Money.create(0))
                .resultAmt(Money.create(0))
                .status(OrderStatus.PENDING)
                .orderedAt(LocalDateTime.now())
                .build();
    }


    /** 할인금액, 최종금액 계산*/
    public void calculateAmtWithCoupon(Money discountAmt) {
        if(this.couponId == null) return;
        else if(this.status != OrderStatus.PENDING) {
            throw new RuntimeException("주문생성시에만 적용 가능");
        }

        // 할인금액
        this.discountAmt = discountAmt;

        // 최종금액
        calculateAmt();
    }

    public void calculateAmt() {
        this.originAmt = orderItems.stream()
                .map(OrderItem::calculateOrderItem)
                .reduce(Money.create(0L), Money::add);

        this.resultAmt = this.originAmt.subtract(this.discountAmt);
    }

    /** todo 주문상태 변경 */

    /**
     * 주문 완료처리
     */
    public void confirm() {
        if(this.status != OrderStatus.PENDING) {
            throw new RuntimeException("주문완료처리 불가");
        }

        if(this.originAmt == Money.create(0)
                || (this.couponId != null && this.discountAmt == Money.create(0))
                || this.originAmt.subtract(this.discountAmt) != this.resultAmt
        ) {
            throw new RuntimeException("주문생성 오류");
        }

        updateOrderStatus(CONFIRMED);
    }

    /** 주문 취소 **/
    public void cancel() {
        if(this.status != OrderStatus.CONFIRMED) {
            throw new RuntimeException("주문 취소처리 불가");
        }

        updateOrderStatus(CANCELED);
    }

    /** 주문 환불 **/
    public void refund() {
        if(this.status != OrderStatus.PAID) {
            throw new RuntimeException("환불처리 불가");
        }

        updateOrderStatus(REFUND);
    }

    /**
     * 주문상태, 수정시간 변경
     * @param updateStatus
     */
    private void updateOrderStatus(OrderStatus updateStatus) {
        this.updatedAt = LocalDateTime.now();
        this.status = updateStatus;
    }
}
