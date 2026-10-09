package com.commerce.product.bootstrap.event;

import com.commerce.product.bootstrap.event.dto.ProductPricingEvent;
import com.commerce.product.core.application.port.out.ProductOutputPort;
import com.commerce.product.core.domain.aggregate.Product;
import com.commerce.product.core.domain.enums.ProductStatus;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.OrderPriceFailedEvent;
import com.commerce.shared.kafka.event.dto.OrderPricedEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProductConsumerTest {

    @Mock ProductOutputPort productOutputPort;
    @Mock TransactionalEventPublisher transactionalEventPublisher;

    OrderProductConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderProductConsumer(productOutputPort, transactionalEventPublisher);
    }

    private ProductPricingEvent sampleEvent() {
        return new ProductPricingEvent(
                "O001", "C001", null,
                List.of(new ItemEntry(ProductId.of("P001"), Quantity.create(2))),
                "CARD", "shinHan", 0
        );
    }

    @DisplayName("inventory.deducted 수신 시 금액을 계산하고 OrderPricedEvent를 발행한다")
    @Test
    void handleSuccess() {
        Product product = Product.builder()
            .productId(ProductId.of("P001")).productName("테스트").price(Money.of(5000)).status(ProductStatus.ACTIVE).build();
        given(productOutputPort.findById(ProductId.of("P001"))).willReturn(Optional.of(product));

        consumer.handleCalculatePricing(sampleEvent());

        verify(transactionalEventPublisher).publish(eq(EventTopic.ORDER_PRICED_TOPIC), any(OrderPricedEvent.class));
    }

    @DisplayName("상품 조회 실패 시 OrderPriceFailedEvent를 발행한다")
    @Test
    void handleFailure() {
        given(productOutputPort.findById(any())).willReturn(Optional.empty());

        consumer.handleCalculatePricing(sampleEvent());

        verify(transactionalEventPublisher).publish(eq(EventTopic.ORDER_PRICE_FAILED_TOPIC), any(OrderPriceFailedEvent.class));
    }
}
