package com.commerce.platform.infrastructure.adaptor;

import com.commerce.platform.core.domain.aggreate.CardBinPromotion;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.vo.promotion.ShinhanPromotionData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class CardBinPromotionAdaptorTest {
    @Autowired
    private CardBinPromotionAdaptor cardBinPromotionAdaptor;

    @Test
    void findActiveByCardBins() {
        List<CardBinPromotion> activePromotions = cardBinPromotionAdaptor.findActivePromotions();

        assertAll(
                () -> {
                    assertThat(activePromotions.size()).isEqualTo(7);
                    assertThat(activePromotions.stream()
                            .filter(p -> p.getPayProvider().equals(PayProvider.SHIN_HAN))
                            .map(CardBinPromotion::getPromotionData)
                            .findFirst()
                            .get()
                    ).isExactlyInstanceOf(ShinhanPromotionData.class);
                }
        );
    }

}