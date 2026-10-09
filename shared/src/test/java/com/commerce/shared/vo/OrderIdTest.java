package com.commerce.shared.vo;

import com.commerce.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderIdTest {

    @DisplayName("OrderId는 'O' + UUID 형식이고 길이가 컬럼 길이와 같다")
    @Test
    void createUsesUuid() {
        OrderId orderId = OrderId.create();

        assertThat(orderId.id()).startsWith("O").hasSize(OrderId.LENGTH);
        assertThat(UUID.fromString(orderId.id().substring(1))).isNotNull();
    }

    @DisplayName("동시에 대량 생성해도 중복되지 않는다")
    @Test
    void createIsUniqueUnderConcurrency() {
        Set<String> ids = ConcurrentHashMap.newKeySet();
        IntStream.range(0, 10_000).parallel().forEach(i -> ids.add(OrderId.create().id()));

        assertThat(ids).hasSize(10_000);
    }

    @DisplayName("'O'로 시작하지 않으면 거부한다")
    @Test
    void rejectsInvalidPrefix() {
        assertThatThrownBy(() -> OrderId.of("X123")).isInstanceOf(BusinessException.class);
    }
}
