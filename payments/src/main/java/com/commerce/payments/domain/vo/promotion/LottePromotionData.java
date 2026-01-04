package com.commerce.payments.domain.vo.promotion;

import lombok.Builder;

@Builder
public record LottePromotionData(
    String lotte_target,
    String lotte_payType,
    String lotte_card_name,
    String lotte_content,
    String lotte_condition
) implements BasePromotionData {
}
