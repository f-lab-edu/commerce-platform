package com.commerce.shared.kafka;

import com.commerce.shared.kafka.event.dto.DomainEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T extends DomainEvent> void publish(EventTopic topic, T event) {
        try {
            // todo send() 에서 key 매개변수의 역할이 무엇인지 확인 필요
            kafkaTemplate.send(topic.getValue(), event.key(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("SUCCESS to publish event - topic: {}, key: {}, eventData: {}",
                                    topic.getValue(), event.key(), event);
                        } else {
                            log.info("FAILED to publish event - topic: {}, key: {}, eventData: {}",
                                    topic.getValue(), event.key(), event);
                        }
                    });
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
