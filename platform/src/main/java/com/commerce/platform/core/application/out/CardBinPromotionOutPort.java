package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.CardBinPromotion;

import java.util.List;

public interface CardBinPromotionOutPort {
    List<CardBinPromotion> findActivePromotions();
}
