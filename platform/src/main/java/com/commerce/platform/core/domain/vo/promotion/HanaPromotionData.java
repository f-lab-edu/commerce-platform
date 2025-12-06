package com.commerce.platform.core.domain.vo.promotion;

import lombok.Builder;

@Builder
public record HanaPromotionData(
    String hana_target,
    String hana_payType,
    String hana_card_name,
    String hana_content,
    String hana_condition
) implements BasePromotionData {
}
