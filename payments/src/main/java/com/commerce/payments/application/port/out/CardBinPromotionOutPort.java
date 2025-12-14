package com.commerce.payments.application.port.out;


import com.commerce.payments.domain.aggregate.CardBinPromotion;

import java.util.List;

public interface CardBinPromotionOutPort {
    List<CardBinPromotion> findActivePromotions();
}
