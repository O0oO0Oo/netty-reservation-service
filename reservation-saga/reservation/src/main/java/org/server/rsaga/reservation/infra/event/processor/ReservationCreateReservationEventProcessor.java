package org.server.rsaga.reservation.infra.event.processor;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.messaging.MessagingTopics;
import org.server.rsaga.messaging.adapter.processor.KafkaMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.factory.KafkaMessageProcessorFactory;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaMessageProcessorType;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.reservation.app.ReservationMessageEventService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class ReservationCreateReservationEventProcessor {
    private final KafkaMessageProcessorFactory processorFactory;
    private final ReservationMessageEventService reservationMessageEventService;

    @Bean
    public KafkaMessageProcessor<String, CreateReservationEvent> createReservationCheckReservationLimitEventKafkaMessageProcessor() {
        Properties config = new Properties();
        config.put("producer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("dead.letter.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());

        config.put("group.id", "create-reservation.reservation.check-limit-consumer");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_CHECK_RESERVATION_LIMIT.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        return processorFactory.create(
                config,
                reservationMessageEventService::consumeCheckReservationLimitEvent,
                KafkaMessageProcessorType.MULTI_THREADED
        );
    }

    @Bean
    public KafkaMessageProcessor<String, CreateReservationEvent> createReservationEventFinalKafkaMessageProcessor() {
        Properties config = new Properties();
        config.put("producer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("dead.letter.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());

        config.put("group.id", "create-reservation.reservation.check-limit-consumer");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_FINAL_STEP.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        return processorFactory.create(
                config,
                reservationMessageEventService::consumeCreateReservationFinalEvent,
                KafkaMessageProcessorType.MULTI_THREADED
        );
    }
}
