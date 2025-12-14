package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.shared.vo.Quantity;
import com.commerce.shared.vo.ValidPeriod;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.commerce.shared.exception.BusinessError.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupon")
@Entity
public class Coupon {
    @EmbeddedId
    private CouponId couponId;

    @Column(name = "name", nullable = false, unique = true, length = 30)
    private String couponName;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "discount_percent", nullable = false)
    private int discountPercent;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "min_order_amt", nullable = false))
    private Money minOrderAmt;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "max_discount_amt", nullable = false))
    private Money maxDiscountAmt;

    @Embedded
    private ValidPeriod validPeriod;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_quantity", nullable = false))
    private Quantity totalQuantity;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "issued_quantity", nullable = false))
    private Quantity issuedQuantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static Coupon create(
            String code,
            String couponName,
            int discountPercent,
            long minOrderAmt,
            long maxDiscountAmt,
            LocalDate frDt,
            LocalDate toDt,
            long totalQuantity
    ) {

        return Coupon.builder()
                .couponId(CouponId.create())
                .couponName(couponName)
                .code(code)
                .discountPercent(discountPercent)
                .minOrderAmt(Money.of(minOrderAmt))
                .maxDiscountAmt(Money.of(maxDiscountAmt))
                .validPeriod(ValidPeriod.create(frDt, toDt))
                .totalQuantity(Quantity.create(totalQuantity))
                .issuedQuantity(Quantity.create(0))
                .build();
    }

    /**
     * 사용가능여부 확인
     */
    public void isAvailable(Money orderAmt) {
        // 주문금액 확인
        if(this.minOrderAmt.isGreaterThan(orderAmt)) throw new BusinessException(BELOW_LEAST_ORDER_AMT);

        // 유효기간 확인
        isValidPeriod();
    }

    /**
     * 발급
     */
    public void issueCoupon() {
        isValidPeriod();

        // 수량 확인
        try {
            this.totalQuantity.minus(this.issuedQuantity.add(Quantity.create(1)));
        } catch (BusinessException be) {
            throw new BusinessException(QUANTITY_EXCEEDED_COUPON);
        }

        // 발급수량 ++
        this.issuedQuantity = Quantity.create(this.issuedQuantity.value() + 1L);
    }

    /**
     * 할인금액 계산
     */
    public Money calculateDiscountAmt(Money orderAmt) {
        isAvailable(orderAmt);

        // 할인금액
        Money discountAmt = orderAmt.discount(this.discountPercent);
        if(discountAmt.isGreaterThan(this.maxDiscountAmt)) {
            return this.maxDiscountAmt;
        }

        return discountAmt;
    }

    private void isValidPeriod() {
        if(!validPeriod.nowInPeriod()) throw new BusinessException(NOT_WITHIN_PERIOD_COUPON);
    }

    /**
     * 테스트용
     */
    public void changeIssuedQuantityForTest(long quantity) {
        this.issuedQuantity = Quantity.create(quantity);
    }

    @Builder
    private Coupon(CouponId couponId, String couponName, String code,
                   int discountPercent, Money minOrderAmt, Money maxDiscountAmt,
                   ValidPeriod validPeriod, Quantity totalQuantity, Quantity issuedQuantity,
                   LocalDateTime createdAt) {
        this.couponId = couponId;
        this.couponName = couponName;
        this.code = code;
        this.discountPercent = discountPercent;
        this.minOrderAmt = minOrderAmt;
        this.maxDiscountAmt = maxDiscountAmt;
        this.validPeriod = validPeriod;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = issuedQuantity;
        this.createdAt = createdAt;
    }
}
