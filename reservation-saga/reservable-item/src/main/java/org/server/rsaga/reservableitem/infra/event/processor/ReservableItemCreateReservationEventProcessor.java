package org.server.rsaga.reservableitem.infra.event.processor;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.messaging.MessagingTopics;
import org.server.rsaga.messaging.adapter.processor.KafkaBulkMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.factory.KafkaMessageProcessorFactory;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaBulkMessageProcessorType;
import org.server.rsaga.reservableitem.app.ReservableItemMessageEventService;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class ReservableItemCreateReservationEventProcessor {
    private final KafkaMessageProcessorFactory processorFactory;
    private final ReservableItemMessageEventService reservableItemMessageEventService;

    @Bean("createReservationEventVerifyReservableItemKafkaBulkMessageProcessor")
    public KafkaBulkMessageProcessor<String, CreateReservationEvent> createReservationEventVerifyReservableItemKafkaBulkMessageProcessor(){
        Properties config = new Properties();
        config.put("producer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("dead.letter.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());

        config.put("group.id", "create-reservation.reservable-item.verify-consumer");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_VERIFY_RESERVABLEITEM.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        return processorFactory.create(
                config,
                reservableItemMessageEventService::consumeBulkVerifyReservableItemEvent,
                KafkaBulkMessageProcessorType.MULTI_THREADED
        );
    }

    @Bean("createReservationEventUpdateReservableItemQuantityKafkaBulkMessageProcessor")
    public KafkaBulkMessageProcessor<String, CreateReservationEvent> createReservationEventUpdateReservableItemQuantityKafkaBulkMessageProcessor() {
        Properties config = new Properties();
        config.put("producer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("dead.letter.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());

        config.put("group.id", "create-reservation.reservable-item.update-quantity-consumer");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_UPDATE_RESERVABLEITEM_QUANTITY.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        return processorFactory.create(
                config,
                reservableItemMessageEventService::consumeBulkUpdateReservableItemQuantityEvent,
                KafkaBulkMessageProcessorType.MULTI_THREADED
        );
    }
}