package com.commerce.shared.kafka;

import com.commerce.shared.kafka.event.dto.DomainEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.kafka.event.type.CancelType;
import com.commerce.shared.kafka.event.type.EventHeaders;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    KafkaEventPublisher kafkaEventPublisher;

    @DisplayName("key와 헤더를 담은 ProducerRecord로 발행한다")
    @Test
    void publishesRecordWithKeyAndHeaders() {
        given(kafkaTemplate.send(any(ProducerRecord.class)))
                .willReturn(new CompletableFuture<SendResult<String, Object>>());
        TestEvent event = new TestEvent("O1", LocalDateTime.now());

        kafkaEventPublisher.publish(EventTopic.PAYMENT_PG_CANCEL_TOPIC, event,
                Map.of(EventHeaders.CANCEL_TYPE, CancelType.REFUND.name()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        ProducerRecord<String, Object> record = captor.getValue();

        assertThat(record.topic()).isEqualTo("payment.pg-cancel");
        assertThat(record.key()).isEqualTo("O1");
        assertThat(record.value()).isEqualTo(event);
        assertThat(new String(record.headers().lastHeader(EventHeaders.CANCEL_TYPE).value(), StandardCharsets.UTF_8))
                .isEqualTo("REFUND");
    }

    @DisplayName("헤더 없이 발행하면 레코드에 헤더가 없다")
    @Test
    void publishesWithoutHeaders() {
        given(kafkaTemplate.send(any(ProducerRecord.class)))
                .willReturn(new CompletableFuture<SendResult<String, Object>>());

        kafkaEventPublisher.publish(EventTopic.ORDER_CREATED_TOPIC, new TestEvent("O1", LocalDateTime.now()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertThat(captor.getValue().headers().toArray()).isEmpty();
    }

    record TestEvent(String key, LocalDateTime timestamp) implements DomainEvent {}
}
