package org.server.rsaga.reservation.infra.event.consumer;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.messaging.MessagingTopics;
import org.server.rsaga.messaging.adapter.consumer.KafkaMessageConsumer;
import org.server.rsaga.messaging.adapter.consumer.factory.KafkaMessageConsumerFactory;
import org.server.rsaga.messaging.adapter.consumer.strategy.KafkaMessageConsumerType;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class ReservationCreateSagaConsumer {
    private final KafkaMessageConsumerFactory kafkaMessageConsumerFactory;

    @Bean("createReservationCoordinatorConsumer")
    public KafkaMessageConsumer<String, CreateReservationEvent> createReservationCoordinatorConsumer() {
        Properties config = new Properties();
        config.put("group.id", "create-reservation-group");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        return kafkaMessageConsumerFactory.createConsumer(
                config, KafkaMessageConsumerType.AT_LEAST_ONCE
        );
    }
}