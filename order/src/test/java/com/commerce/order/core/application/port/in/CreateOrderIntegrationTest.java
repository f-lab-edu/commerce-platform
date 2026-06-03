package com.commerce.order.core.application.port.in;

import com.commerce.order.core.application.port.in.dto.CreateOrderCommand;
import com.commerce.order.core.application.port.in.dto.OrderResponse;
import com.commerce.order.core.domain.aggregate.Order;
import com.commerce.order.core.domain.enums.OrderStatus;
import com.commerce.order.infrastructure.persistence.OrderItemRepository;
import com.commerce.order.infrastructure.persistence.OrderRepository;
import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.event.dto.OrderCreatedEvent;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.vo.OrderId;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static com.commerce.shared.kafka.event.topic.EventTopic.ORDER_CREATED_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 이벤트 발행 검증
 */
@SpringBootTest
class CreateOrderIntegrationTest {

    private static final String TOPIC = ORDER_CREATED_TOPIC.getValue();
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    @Autowired OrderUseCase orderUseCase;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;
    @Autowired ObjectMapper objectMapper;

    @Value("${spring.kafka.bootstrap-servers}")
    String bootstrapServers;

    private KafkaConsumer<String, String> kafkaConsumer;
    private final List<OrderId> createdIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "create-order-int-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(List.of(TOPIC));

        // partition assignment 완료까지 대기 + 토픽 끝으로 명시적 seek
        // (auto.offset.reset=latest만으로는 join/assignment 타이밍에 따라 직후 produce된 메시지를 놓칠 수 있음)
        long deadline = System.currentTimeMillis() + 5000;
        while (kafkaConsumer.assignment().isEmpty() && System.currentTimeMillis() < deadline) {
            kafkaConsumer.poll(Duration.ofMillis(200));
        }
        if (kafkaConsumer.assignment().isEmpty()) {
            throw new IllegalStateException("Kafka partition assignment timed out for " + TOPIC);
        }
        kafkaConsumer.seekToEnd(kafkaConsumer.assignment());
        // seekToEnd는 lazy → position() 호출로 즉시 적용
        kafkaConsumer.assignment().forEach(kafkaConsumer::position);
    }

    @AfterEach
    void cleanup() {
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
        for (OrderId id : createdIds) {
            orderItemRepository.deleteAll(orderItemRepository.findByOrderId(id));
            orderRepository.deleteById(id);
        }
        createdIds.clear();
    }

    @DisplayName("createOrder 성공: Order가 DB에 저장되고 OrderCreatedEvent가 발행된다")
    @Test
    void createOrderSuccessPersistsAndPublishes() {
        String custId = uniqueCustomerId();
        CreateOrderCommand command = new CreateOrderCommand(
                CustomerId.of(custId), null,
                List.of(new CreateOrderCommand.OrderItemCommand(ProductId.of("P001"), Quantity.create(2))),
                "CARD", "shinHan"
        );

        OrderResponse response = orderUseCase.createOrder(command);
        createdIds.add(OrderId.of(response.orderId()));

        Order persisted = orderRepository.findById(OrderId.of(response.orderId())).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(persisted.getCustomerId().id()).isEqualTo(custId);
        assertThat(persisted.getOriginAmt().value()).isEqualTo(0L);
        assertThat(persisted.getDiscountAmt().value()).isEqualTo(0L);

        OrderCreatedEvent event = pollForEvent(
                e -> response.orderId().equals(e.orderId()),
                Duration.ofSeconds(5)
        );
        assertThat(event).as("order.created 이벤트 발행 확인").isNotNull();
        assertThat(event.customerId()).isEqualTo(custId);
        assertThat(event.couponId()).isNull();
        assertThat(event.payMethod()).isEqualTo("CARD");
        assertThat(event.payProvider()).isEqualTo("shinHan");
        assertThat(event.items()).hasSize(1);
        assertThat(event.items().get(0).productId()).isEqualTo(ProductId.of("P001"));
        assertThat(event.items().get(0).quantity()).isEqualTo(Quantity.create(2));
    }

    @DisplayName("createOrder 실패: 빈 items로 INVALID_REQUEST_VALUE 발생 시 DB 저장도 이벤트 발행도 없다")
    @Test
    void createOrderFailureDoesNotPublishEvent() {
        String custId = uniqueCustomerId();
        CreateOrderCommand command = new CreateOrderCommand(
                CustomerId.of(custId), null,
                List.of(),
                "CARD", "shinHan"
        );

        assertThatThrownBy(() -> orderUseCase.createOrder(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", BusinessError.INVALID_REQUEST_VALUE.getCode());

        OrderCreatedEvent event = pollForEvent(
                e -> custId.equals(e.customerId()),
                Duration.ofSeconds(2)
        );
        assertThat(event).as("실패 시 이벤트 미발행").isNull();

        assertThat(orderRepository.findByCustomerId(CustomerId.of(custId)))
                .as("실패 시 DB에 주문 저장 없음")
                .isEmpty();
    }

    private String uniqueCustomerId() {
        return "CINT" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 주어진 predicate에 매칭되는 OrderCreatedEvent를 timeout 내에 polling 한다.
     * 매칭되는 이벤트가 없으면 null 반환.
     */
    private OrderCreatedEvent pollForEvent(Predicate<OrderCreatedEvent> match, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(POLL_INTERVAL);
            for (ConsumerRecord<String, String> record : records) {
                try {
                    OrderCreatedEvent event = objectMapper.readValue(record.value(), OrderCreatedEvent.class);
                    if (match.test(event)) {
                        return event;
                    }
                } catch (Exception ignored) {
                    // 다른 스키마(스트레이 메시지)는 skip
                }
            }
        }
        return null;
    }
}
