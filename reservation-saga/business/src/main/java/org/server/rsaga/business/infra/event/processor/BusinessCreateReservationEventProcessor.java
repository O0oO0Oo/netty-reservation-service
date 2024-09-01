package org.server.rsaga.business.infra.event.processor;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.business.app.BusinessMessageEventService;
import org.server.rsaga.common.messaging.MessagingTopics;
import org.server.rsaga.messaging.adapter.processor.KafkaBulkMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.factory.KafkaMessageProcessorFactory;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaBulkMessageProcessorType;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class BusinessCreateReservationEventProcessor {
    private final KafkaMessageProcessorFactory processorFactory;
    private final BusinessMessageEventService businessMessageEventService;

    @Bean
    public KafkaBulkMessageProcessor<String, CreateReservationEvent> createReservationEventKafkaBulkMessageProcessor(){
        Properties config = new Properties();
        config.put("producer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("dead.letter.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());

        config.put("group.id", "create-reservation.business.verify-consumer");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_VERIFY_BUSINESS.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        return processorFactory.create(
                config,
                businessMessageEventService::consumeBulkVerifyBusinessEvents,
                KafkaBulkMessageProcessorType.MULTI_THREADED
        );
    }
}