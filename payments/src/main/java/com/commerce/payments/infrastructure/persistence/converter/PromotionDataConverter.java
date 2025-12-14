package com.commerce.payments.infrastructure.persistence.converter;

import com.commerce.payments.domain.vo.promotion.BasePromotionData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.log4j.Log4j2;

/**
 * BasePromotionData <-> JSON String 변환 Converter
 */
@Log4j2
@Converter
public class PromotionDataConverter implements AttributeConverter<BasePromotionData, String> {
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    @Override
    public String convertToDatabaseColumn(BasePromotionData attribute) {
        if (attribute == null) return null;
        
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("프로모션 데이터 직렬화 실패", e);
        }
    }
    
    @Override
    public BasePromotionData convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        
        // 임시 객체 JsonPromotionData 반환
        return new JsonPromotionData(dbData);
    }

    /**
     * JSON String을 보관하는 임시 래퍼 클래스
     */
    public record JsonPromotionData(String jsonData)
            implements BasePromotionData {}
}
