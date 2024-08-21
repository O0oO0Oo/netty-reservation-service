package org.server.rsaga.user.infra.event.processor;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.messaging.MessagingTopics;
import org.server.rsaga.messaging.adapter.processor.KafkaMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.factory.KafkaMessageProcessorFactory;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaMessageProcessorType;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.user.app.UserMessageEventService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class UserCreateReservationEventProcessor {
    private final KafkaMessageProcessorFactory processorFactory;
    private final UserMessageEventService userMessageEventService;

    @Bean
    public KafkaMessageProcessor<String, CreateReservationEvent> createReservationEventVerifyUserKafkaMessageProcessor() {
        Properties config = new Properties();
        config.put("producer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("dead.letter.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());

        config.put("group.id", "create-reservation.user.verify-consumer");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_VERIFY_USER.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        return processorFactory.create(
                config,
                userMessageEventService::consumerVerifyUserEvent,
                KafkaMessageProcessorType.MULTI_THREADED
        );
    }
}