package com.commerce.payments.core.application.port.out;


import com.commerce.payments.core.domain.aggregate.CardBinPromotion;

import java.util.List;

public interface CardBinPromotionOutPort {
    List<CardBinPromotion> findActivePromotions();
}
