package com.commerce.platform.core.domain.vo.promotion;

import lombok.Builder;

@Builder
public record SamsungPromotionData(
    String samsung_target,
    String samsung_payType,
    String samsung_card_name,
    String samsung_content,
    String samsung_condition
) implements BasePromotionData {
}
