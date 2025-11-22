package com.commerce.platform.infrastructure.adaptor;

import com.commerce.platform.core.application.out.CardBinPromotionOutPort;
import com.commerce.platform.core.domain.aggreate.CardBinPromotion;
import com.commerce.platform.infrastructure.persistence.CardBinPromotionRepository;
import com.commerce.platform.infrastructure.persistence.processor.PromotionDataPostProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * promotionData 후처리
 * CardBinPromotion 반환
 */
@Log4j2
@RequiredArgsConstructor
@Component
public class CardBinPromotionAdaptor implements CardBinPromotionOutPort {
    
    private final CardBinPromotionRepository repository;
    private final PromotionDataPostProcessor postProcessor;

    public List<CardBinPromotion> findActivePromotions() {
        List<CardBinPromotion> results = repository.findAllByActive();
        results.forEach(postProcessor::process);
        return results;
    }

    public CardBinPromotion save(CardBinPromotion entity) {
        CardBinPromotion saved = repository.save(entity);
        postProcessor.process(saved);
        return saved;
    }
}
