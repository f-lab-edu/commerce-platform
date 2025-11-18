package com.commerce.platform.core.domain.vo.promotion;

import lombok.Builder;

@Builder
public record BcPromotionData(
    String bc_target,
    String bc_payType,
    String bc_card_name,
    String bc_content,
    String bc_condition
) implements BasePromotionData {
}
