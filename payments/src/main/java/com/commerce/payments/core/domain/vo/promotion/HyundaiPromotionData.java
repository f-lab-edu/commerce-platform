package com.commerce.payments.core.domain.vo.promotion;

import lombok.Builder;

@Builder
public record HyundaiPromotionData(
    String hyundai_target,
    String hyundai_payType,
    String hyundai_card_name,
    String hyundai_content,
    String hyundai_condition
) implements BasePromotionData {
}
