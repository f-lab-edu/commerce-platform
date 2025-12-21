package com.commerce.payments.infrastructure.persistence.processor;

import com.commerce.payments.domain.aggregate.CardBinPromotion;
import com.commerce.payments.domain.vo.promotion.*;
import com.commerce.shared.enums.PayProvider;
import com.commerce.payments.infrastructure.persistence.converter.PromotionDataConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * CardBinPromotion 조회 후 PayProvider에 따른 promotionData 타입 변환
 */
@Log4j2
@Component
public class PromotionDataPostProcessor {
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public void process(CardBinPromotion entity) {
        if (entity == null || entity.getPayProvider() == null) return;
        
        BasePromotionData currentData = entity.getPromotionData();
        if (currentData == null) return;
        
        // 카드사에 매핑되는 프로모션 dto 변환
        if (currentData instanceof PromotionDataConverter.JsonPromotionData(String jsonData)) {
            try {
                BasePromotionData convertedData = convertToProperType(jsonData, entity.getPayProvider());
                setPromotionData(entity, convertedData);
                
            } catch (Exception e) {
                log.error("프로모션 데이터 후처리 실패 -  PayProvider: {}", entity.getPayProvider(), e);
            }
        }
    }

    public BasePromotionData convertToProperType(String json, PayProvider payProvider) throws JsonProcessingException {
        if (json == null || json.isBlank()) return null;
        
        Class<? extends BasePromotionData> clazz = getPromotionDataClass(payProvider);
        return objectMapper.readValue(json, clazz);
    }

    private Class<? extends BasePromotionData> getPromotionDataClass(PayProvider payProvider) {
        return switch (payProvider) {
            case SAMSUNG -> SamsungPromotionData.class;
            case SHIN_HAN -> ShinhanPromotionData.class;
            case KB -> KbPromotionData.class;
            case HYUNDAI -> HyundaiPromotionData.class;
            case BC -> BcPromotionData.class;
            case HANA -> HanaPromotionData.class;
            case LOTTE -> LottePromotionData.class;
            case NH -> NhPromotionData.class;
            default -> throw new IllegalArgumentException(
                "지원하지 않는 카드사입니다: " + payProvider
            );
        };
    }

    private void setPromotionData(CardBinPromotion entity, BasePromotionData data) {
        try {
            Field field = CardBinPromotion.class.getDeclaredField("promotionData");
            field.setAccessible(true);
            field.set(entity, data);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("promotionData 필드 설정 실패", e);
            throw new IllegalStateException("promotionData 필드 설정 실패", e);
        }
    }
}
