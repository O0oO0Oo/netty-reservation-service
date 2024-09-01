package org.server.rsaga.messaging.adapter.processor.strategy;

import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.server.rsaga.messaging.adapter.consumer.util.KafkaConsumerPropertiesUtil;
import org.server.rsaga.messaging.adapter.processor.KafkaSingleMessageProcessor;
import org.server.rsaga.messaging.adapter.processor.impl.MultiThreadedKafkaSingleMessageProcessor;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.factory.KafkaMessageProducerFactory;
import org.server.rsaga.messaging.adapter.producer.strategy.KafkaMessageProducerType;
import org.server.rsaga.messaging.adapter.producer.util.KafkaDeadLetterHandler;
import org.server.rsaga.messaging.adapter.util.KafkaTopicUtils;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.util.DeadLetterHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class MultiThreadedKafkaSingleMessageProcessorCreationStrategy implements KafkaSingleMessageProcessorCreationStrategy {
    private final KafkaConsumerPropertiesUtil kafkaConsumerPropertiesUtil;
    private final KafkaTopicUtils kafkaTopicUtils;
    private final KafkaMessageProducerFactory kafkaMessageProducerFactory;

    /**
     * <pre>
     * List<KafkaConsumer<K, V>> kafkaConsumers - 만들어서 넣어야함.
     * KafkaMessageProducer<K, V> kafkaMessageProducer - 만들어서
     * DeadLetterHandler<K, V> deadLetterHandler - 만들어서, 위의 프로듀서랑 동일
     * String consumeTopic - 받아서
     * String produceTopic - 받아서
     * </pre>
     * @return {@link KafkaMessageProducer} 카프카 메시지 프로듀서 반환
     */
    @Override
    public <K, V> KafkaSingleMessageProcessor<K, V> createMessageProcessor(Properties config,
                                                                           UnaryOperator<Message<K, V>> operator
    ) {
        checkConfig(config);

        String producerTopic = (String) config.remove("producer.topic");
        String deadLetterTopic = (String) config.remove("dead.letter.topic");

        // consumer
        List<KafkaConsumer<K, V>> kafkaConsumers = createConsumers(config);

        // producer
        KafkaMessageProducer<K, V> messageProducer = createProducer(new Properties());

        // dead letter handler
        DeadLetterHandler<K, V> deadLetterHandler = createDeadLetterHandler(messageProducer, deadLetterTopic);

        // processor
        MultiThreadedKafkaSingleMessageProcessor<K, V> messageProcessor = new MultiThreadedKafkaSingleMessageProcessor<>(
                kafkaConsumers,
                messageProducer,
                deadLetterHandler,
                producerTopic
        );
        messageProcessor.registerHandler(operator);
        return messageProcessor;
    }

    /**
     * @return 토픽의 파티션 수 만큼 컨슈머를 만든다.
     */
    private <K, V> List<KafkaConsumer<K, V>> createConsumers(Properties config) {
        String consumerTopic = (String) config.remove("consumer.topic");
        List<KafkaConsumer<K, V>> consumers = new ArrayList<>();

        int partitionCount = kafkaTopicUtils.getPartitionCount(consumerTopic);

        for (int i = 0; i < partitionCount; i++) {
            Properties consumerProperties = kafkaConsumerPropertiesUtil.atLeastOnceProtobufConsumerProperties(config);
            KafkaConsumer<K, V> kafkaConsumer = new KafkaConsumer<>(consumerProperties);
            consumers.add(kafkaConsumer);
        }

        for (KafkaConsumer<K, V> consumer : consumers) {
            consumer.subscribe(Collections.singleton(consumerTopic));
        }

        return consumers;
    }

    private <K, V> KafkaMessageProducer<K, V> createProducer(Properties config) {
        // at least once batch producer
        return kafkaMessageProducerFactory.createProducer(
                config,
                KafkaMessageProducerType.AT_LEAST_ONCE_BATCH
        );
    }

    private <K, V> DeadLetterHandler<K, V> createDeadLetterHandler(KafkaMessageProducer<K, V> messageProducer, String topic) {
        return new KafkaDeadLetterHandler<>(messageProducer, topic);
    }

    @Override
    public void checkConfig(Properties config) {
        isContainsKey(config, "dead.letter.topic");

        isContainsKey(config, "producer.topic");

        isContainsKey(config, ConsumerConfig.GROUP_ID_CONFIG);
        isContainsKey(config, KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE);
        isContainsKey(config, "consumer.topic");
    }

    private void isContainsKey(Properties config, String key) {
        if (!config.containsKey(key)) {
            throw new IllegalArgumentException("config must have a key of " + key);
        }
    }

    @Override
    public KafkaSingleMessageProcessorType getType() {
        return KafkaSingleMessageProcessorType.MULTI_THREADED;
    }
}