package com.commerce.order.core.domain.aggregate;

import com.commerce.order.core.domain.enums.OrderStatus;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.OrderCreatedEvent;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.OrderId;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.commerce.shared.exception.BusinessError.INVALID_ORDER_STATUS;
import static com.commerce.shared.exception.BusinessError.INVALID_REQUEST_VALUE;

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
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 애그리거트 내부 컬렉션. JPA 매핑 대신 도메인이 메모리에서만 보유한다.
     * createOrder 흐름에서만 채워지며, JPA로 로드한 Order에서는 비어있다.
     */
    @Transient
    private List<OrderItem> items = List.of();


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
            CouponId couponId,
            List<ItemSpec> itemSpecs
    ) {
        if (itemSpecs == null || itemSpecs.isEmpty()) {
            throw new BusinessException(INVALID_REQUEST_VALUE);
        }

        OrderId orderId = OrderId.create();
        List<OrderItem> items = itemSpecs.stream()
                .map(spec -> OrderItem.create(orderId, spec.productId(), spec.quantity()))
                .toList();

        Order order = Order.builder()
                .orderId(orderId)
                .customerId(customerId)
                .couponId(couponId)
                .discountAmt(Money.of(0))
                .originAmt(Money.of(0))
                .resultAmt(Money.of(0))
                .status(OrderStatus.PENDING)
                .orderedAt(LocalDateTime.now())
                .build();
        order.items = items;
        return order;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    /**
     * 주문 생성 이벤트 페이로드 조립. 외부 컨텍스트(결제 수단/PG)만 인자로 받는다.
     */
    public OrderCreatedEvent toCreatedEvent(String payMethod, String payProvider) {
        List<ItemEntry> entries = items.stream()
                .map(oi -> new ItemEntry(oi.getProductId().id(), oi.getQuantity().value()))
                .toList();
        return new OrderCreatedEvent(
                orderId.id(),
                customerId.id(),
                couponId != null ? couponId.id() : null,
                entries,
                payMethod,
                payProvider,
                orderId.id(),
                LocalDateTime.now()
        );
    }

    public record ItemSpec(ProductId productId, Quantity quantity) {}

    /**
     * 결제 완료 통지 시 금액을 세팅한다.
     * 외부에서 전달받은 원금/할인 금액을 Order에 반영하고 결제 금액을 계산한다.
     */
    public void applyAmounts(Money originAmt, Money discountAmt) {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException(INVALID_ORDER_STATUS);
        }
        this.originAmt = originAmt;
        this.discountAmt = discountAmt;
        this.resultAmt = originAmt.subtract(discountAmt);
    }

    /**
     * 주문 확정 - 금액이 세팅된 상태에서 CONFIRMED로 전이
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException(INVALID_ORDER_STATUS);
        }
        if (this.originAmt.value() == 0) {
            throw new BusinessException(INVALID_ORDER_STATUS);
        }
        updateOrderStatus(OrderStatus.CONFIRMED);
    }

    /**
     * 주문 취소 CONFIRMED 상태에서만 CANCELED로 전이한다.
     * 사용자 취소(cancelOrder)와 saga 거절 콜백(orderRejected) 모두에서 호출된다.
     */
    public void cancel() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new BusinessException(INVALID_ORDER_STATUS);
        }
        updateOrderStatus(OrderStatus.CANCELED);
    }

    /** 주문 결제 **/
    public void validForPay() {
        if(this.status != OrderStatus.CONFIRMED) {
            throw new BusinessException(INVALID_ORDER_STATUS);
        }
    }

    /**
     * 주문상태, 수정시간 변경
     */
    private void updateOrderStatus(OrderStatus updateStatus) {
        this.updatedAt = LocalDateTime.now();
        this.status = updateStatus;
    }
}
