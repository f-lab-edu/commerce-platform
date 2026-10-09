package com.commerce.shared.kafka;

import com.commerce.shared.kafka.event.dto.DomainEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T extends DomainEvent> void publish(EventTopic topic, T event) {
        publish(topic, event, Map.of());
    }

    /**
     * 헤더를 포함해 발행한다. 같은 토픽에 여러 처리 유형이 섞일 때(예: payment.pg-cancel 의 cancelType) 사용한다.
     */
    public <T extends DomainEvent> void publish(EventTopic topic, T event, Map<String, String> headers) {
        try {
            // key = orderId → 같은 주문의 이벤트는 같은 파티션으로 가서 순서가 보장된다
            ProducerRecord<String, Object> record = new ProducerRecord<>(topic.getValue(), event.key(), event);
            headers.forEach((name, value) -> record.headers().add(name, value.getBytes(StandardCharsets.UTF_8)));

            kafkaTemplate.send(record)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("SUCCESS to publish event - topic: {}, key: {}, headers: {}, eventData: {}",
                                    topic.getValue(), event.key(), headers, event);
                        } else {
                            log.info("FAILED to publish event - topic: {}, key: {}, headers: {}, eventData: {}",
                                    topic.getValue(), event.key(), headers, event);
                        }
                    });
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
