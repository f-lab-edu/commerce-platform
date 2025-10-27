package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.Quantity;
import com.commerce.platform.core.domain.vo.ValidPeriod;
import com.commerce.platform.shared.exception.BusinessException;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

import static com.commerce.platform.shared.exception.BusinessError.*;

@Getter
public class Coupon {
    private String couponId;
    private String code;
    private int discountPercent;
    private Money minOrderAmt;
    private Money maxDiscountAmt;
    private ValidPeriod validPeriod;
    private Quantity totalQuantity;
    private Quantity remainQuantity;

    public static Coupon create(
            String code,
            int discountPercent,
            int minOrderAmt,
            int maxDiscountAmt,
            LocalDate frDt,
            LocalDate toDt,
            int totalCnt
    ) throws Exception {
        Coupon coupon = new Coupon();
        coupon.couponId = String.valueOf(UUID.randomUUID());
        coupon.code = code;
        coupon.discountPercent = discountPercent;
        coupon.minOrderAmt = Money.create(minOrderAmt);
        coupon.maxDiscountAmt = Money.create(maxDiscountAmt);
        coupon.validPeriod = ValidPeriod.create(frDt, toDt);
        coupon.totalQuantity = Quantity.create(totalCnt);
        coupon.remainQuantity = coupon.totalQuantity;

        return coupon;
    }

    /**
     * 쿠폰 다운로드 가능여부 확인
     */
    public void valid(Money orderAmt) {
        // 쿠폰 수량 확인
//        this.remainQuantity.minus(Quantity.create(1));

        // 주문금액 확인
        if(this.minOrderAmt.value() > orderAmt.value()) throw new BusinessException(BELOW_LEAST_ORDER_AMT);

        // 유효기간 확인
        if(LocalDate.now().isBefore(this.validPeriod.frDt())) throw new BusinessException(NOT_WITHIN_PERIOD_COUPON);
    }

    // todo 쿠폰 소진

    /**
     * 쿠폰적용
     */
    public Money useCoupon(Money orderAmt) {
        valid(orderAmt);

        // 할인금액
        Money discountAmt = orderAmt.discount(this.discountPercent);
        if(discountAmt.value() > this.maxDiscountAmt.value()) {
            return this.maxDiscountAmt;
        }

        return discountAmt;
    }
}
