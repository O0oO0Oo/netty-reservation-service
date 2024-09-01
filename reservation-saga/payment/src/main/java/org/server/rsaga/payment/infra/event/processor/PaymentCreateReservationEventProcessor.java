package org.server.rsaga.payment.infra.event.processor;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.messaging.MessagingTopics;
import org.server.rsaga.messaging.adapter.processor.KafkaBulkMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.factory.KafkaMessageProcessorFactory;
import org.server.rsaga.messaging.adapter.processor.strategy.KafkaBulkMessageProcessorType;
import org.server.rsaga.payment.app.PaymentMessageEventService;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class PaymentCreateReservationEventProcessor {
    private final KafkaMessageProcessorFactory processorFactory;
    private final PaymentMessageEventService paymentMessageEventService;

    @Bean
    public KafkaBulkMessageProcessor<String, CreateReservationEvent> createReservationEventPaymentKafkaBulkMessageProcessor() {
        Properties config = new Properties();
        config.put("producer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("dead.letter.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());

        config.put("group.id", "create-reservation.payment.payment-consumer");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_PAYMENT.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        return processorFactory.create(
                config,
                paymentMessageEventService::consumeBulkPaymentEvent,
                KafkaBulkMessageProcessorType.MULTI_THREADED
        );
    }
}
