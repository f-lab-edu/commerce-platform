package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.CouponView;
import com.commerce.platform.core.application.out.CouponIssueOutPort;
import com.commerce.platform.core.application.out.CouponOutPort;
import com.commerce.platform.core.domain.aggreate.Coupon;
import com.commerce.platform.core.domain.aggreate.CouponIssue;
import com.commerce.platform.core.domain.vo.CouponId;
import com.commerce.platform.core.domain.vo.CustomerId;
import com.commerce.platform.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.commerce.platform.shared.exception.BusinessError.DUPLICATE_ISSUED_COUPON;
import static com.commerce.platform.shared.exception.BusinessError.INVALID_COUPON;

@RequiredArgsConstructor
@Service
public class CouponIssueUseCaseImpl implements CouponIssueUseCase{
    private final CouponOutPort couponOutPort;
    private final CouponIssueOutPort couponIssueOutPort;

    @Override
    public List<CouponView> getMyCoupons(CustomerId customerId) {
        List<CouponIssue> myCoupons = couponIssueOutPort.findByCustomerId(customerId);

        if(myCoupons.isEmpty()) return List.of();

        List<CouponId> couponIds = myCoupons.stream()
                .map(CouponIssue::getCouponId)
                .toList();

        Map<CouponId, Coupon> couponMap = couponOutPort.findByIdIn(couponIds)
                .stream()
                .collect(Collectors.toMap(Coupon::getCouponId, Function.identity()));

        return myCoupons.stream()
                .map(item -> {
                    Coupon coupon = couponMap.get(item.getCouponId());
                    return new CouponView(coupon.getCouponName(),
                            coupon.getDiscountPercent(),
                            coupon.getMinOrderAmt().value(),
                            coupon.getMaxDiscountAmt().value(),
                            item.getStatus().getValue(),
                            LocalDate.now().until(coupon.getValidPeriod().toDt()).getDays());
                })
                .toList();
    }

    @Override
    public void issueCoupon(CouponId couponId, CustomerId customerId) {
        // 이미 다운받았는지 확인
        couponIssueOutPort.findByIdCustomerId(couponId, customerId)
                .ifPresent(couponIssue -> {
                    throw new BusinessException(DUPLICATE_ISSUED_COUPON);
                });

        Coupon coupon = couponOutPort.findById(couponId)
                .orElseThrow(() -> new BusinessException(INVALID_COUPON));

        // 발행
        coupon.issueCoupon();
        couponIssueOutPort.save(CouponIssue.create(couponId, customerId));
    }
}
