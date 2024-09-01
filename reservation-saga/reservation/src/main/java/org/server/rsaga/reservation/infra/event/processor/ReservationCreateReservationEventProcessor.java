package org.server.rsaga.reservation.infra.event.processor;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.messaging.MessagingTopics;
import org.server.rsaga.messaging.adapter.processor.KafkaBulkMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.KafkaSingleMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.factory.KafkaMessageProcessorFactory;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaBulkMessageProcessorType;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaSingleMessageProcessorType;
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
    public KafkaBulkMessageProcessor<String, CreateReservationEvent> createReservationCheckReservationLimitEventKafkaBulkMessageProcessor() {
        Properties config = new Properties();
        config.put("producer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("dead.letter.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());

        config.put("group.id", "create-reservation.reservation.check-limit-consumer");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_CHECK_RESERVATION_LIMIT.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        return processorFactory.create(
                config,
                reservationMessageEventService::consumeBulkCheckReservationLimitEvent,
                KafkaBulkMessageProcessorType.MULTI_THREADED
        );
    }

    @Bean
    public KafkaBulkMessageProcessor<String, CreateReservationEvent> createReservationEventFinalKafkaBulkMessageProcessor() {
        Properties config = new Properties();
        config.put("producer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("dead.letter.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());

        config.put("group.id", "create-reservation.reservation.final-consumer");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_FINAL_STEP.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        return processorFactory.create(
                config,
                reservationMessageEventService::consumeBulkCreateReservationFinalEvent,
                KafkaBulkMessageProcessorType.MULTI_THREADED
        );
    }
}
