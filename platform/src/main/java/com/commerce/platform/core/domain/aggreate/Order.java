package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.OrderStatus;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.OrderId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.commerce.platform.core.domain.enums.OrderStatus.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order {
    @EmbeddedId
    private OrderId orderId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "customer_id", nullable = false, length = 21))
    private CustomerId customerId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "coupon_id", length = 21))
    private CouponId couponId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "origin_amt"))
    private Money originAmt;     // 할인전금액

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "discount_amt"))
    private Money discountAmt;   // 할인금액

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "result_amt"))
    private Money resultAmt;     // 최종금액

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 4)
    private OrderStatus status;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Builder
    private Order(
            OrderId orderId,
            CustomerId customerId,
            CouponId couponId,
            Money originAmt,
            Money discountAmt,
            Money resultAmt,
            OrderStatus status,
            LocalDateTime orderedAt,
            LocalDateTime updatedAt
    ) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.couponId = couponId;
        this.originAmt = originAmt;
        this.discountAmt = discountAmt;
        this.resultAmt = resultAmt;
        this.status = status;
        this.orderedAt = orderedAt;
        this.updatedAt = updatedAt;
    }

    public static Order create(
            CustomerId customerId,
            CouponId couponId
    ) {
        return Order.builder()
                .orderId(OrderId.create())
                .customerId(customerId)
                .couponId(couponId)
                .discountAmt(Money.create(0))
                .originAmt(Money.create(0))
                .resultAmt(Money.create(0))
                .status(OrderStatus.PENDING)
                .orderedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 주문 완료처리
     * 할인금액, 최종금액 계산
     */
    public void confirm(Money total, Money discount) {
        if(this.status != OrderStatus.PENDING) {
            throw new RuntimeException("주문완료처리 불가");
        }

        if(total == Money.create(0)
                || (this.couponId != null && discount == Money.create(0))
        ) {
            throw new RuntimeException("주문생성 오류");
        }

        this.originAmt = total;
        this.discountAmt = discount;
        this.resultAmt = total.subtract(discount);
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
        updateOrderStatus(REFUND);
    }

    /** 환불가능여부 확인 **/
    public void validateForCancel() {
        if(this.status != OrderStatus.PAID) {
            throw new RuntimeException("환불 불가능한 주문 상태입니다");
        }
    }

    /** 주문 결제 **/
    public void validForPay() {
        if(this.status != OrderStatus.CONFIRMED) {
            throw new RuntimeException("결제처리 불가");
        }
    }

    public void changeStatusAfterPay(PgPayResponse pgPayResponse) {
        if(!pgPayResponse.isSuccess()) return;

        updateOrderStatus(PAID);
    }

    /**
     * 주문상태, 수정시간 변경
     */
    private void updateOrderStatus(OrderStatus updateStatus) {
        this.updatedAt = LocalDateTime.now();
        this.status = updateStatus;
    }
}
