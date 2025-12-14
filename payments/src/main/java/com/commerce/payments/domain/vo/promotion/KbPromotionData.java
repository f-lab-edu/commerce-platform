package com.commerce.payments.domain.vo.promotion;

import lombok.Builder;

@Builder
public record KbPromotionData(
    String kb_target,
    String kb_payType,
    String kb_card_name,
    String kb_content,
    String kb_condition
) implements BasePromotionData {
}
