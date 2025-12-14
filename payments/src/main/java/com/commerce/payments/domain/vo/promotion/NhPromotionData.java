package com.commerce.payments.domain.vo.promotion;

import lombok.Builder;

@Builder
public record NhPromotionData(
    String nh_target,
    String nh_payType,
    String nh_card_name,
    String nh_content,
    String nh_condition
) implements BasePromotionData {
}
