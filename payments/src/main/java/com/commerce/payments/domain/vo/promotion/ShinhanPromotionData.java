package com.commerce.payments.domain.vo.promotion;

import lombok.Builder;

@Builder
public record ShinhanPromotionData(
    String shinhan_target,
    String shinhan_payType,
    String shinhan_card_name,
    String shinhan_content,
    String shinhan_condition
) implements BasePromotionData {
}
