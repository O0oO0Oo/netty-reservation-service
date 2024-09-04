package org.server.rsaga.messaging.adapter.processor.impl;

import com.google.protobuf.DynamicMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.server.rsaga.messaging.adapter.message.KafkaMessage;
import org.server.rsaga.messaging.adapter.processor.KafkaBulkMessageProcessor;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.adapter.util.KafkaDynamicMessageConverter;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.util.DeadLetterHandler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

/**
 * 예외가 발생하면 한번에 소비하는 전체 메시지가 중단되므로, 예외보단 실패 응답으로 처리해야한다.
 */
@Slf4j
public class MultiThreadedKafkaBulkMessageProcessor<K, V> implements KafkaBulkMessageProcessor<K, V> {

    private final List<KafkaConsumer<K, V>> kafkaConsumers;
    private final KafkaMessageProducer<K, V> kafkaMessageProducer;
    private UnaryOperator<List<Message<K, V>>> messageHandler;
    private final DeadLetterHandler<K, V> deadLetterHandler;
    private final ExecutorService threadPool;
    private final String produceTopic;

    public MultiThreadedKafkaBulkMessageProcessor(List<KafkaConsumer<K, V>> kafkaConsumers,
                                                  KafkaMessageProducer<K, V> kafkaMessageProducer,
                                                  DeadLetterHandler<K, V> deadLetterHandler,
                                                  String produceTopic
    ) {
        this.kafkaConsumers = kafkaConsumers;
        this.deadLetterHandler = deadLetterHandler;
        this.kafkaMessageProducer = kafkaMessageProducer;
        this.threadPool = Executors.newFixedThreadPool(kafkaConsumers.size());
        this.produceTopic = produceTopic;
    }

    @Override
    public void registerHandler(UnaryOperator<List<Message<K, V>>> messageHandler) {
        this.messageHandler = messageHandler;
        for (KafkaConsumer<K, V> kafkaConsumer : kafkaConsumers) {
            threadPool.submit(() -> processMessages(kafkaConsumer));
        }
    }

    private void processMessages(KafkaConsumer<K, V> kafkaConsumer) {
        while (true) {
            try {
                ConsumerRecords<K, V> messageRecords = kafkaConsumer.poll(Duration.ofMillis(200));
                if(!messageRecords.isEmpty()) {
                    processRecord(messageRecords);
                }
            } catch (Exception e) {
                log.error("Exception occurred. cause : {}", e.getMessage());
                waitMoment();
            }
        }
    }

    // 대기 후 재시도를 위한 로직
    private void waitMoment() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processRecord(ConsumerRecords<K, V> messageRecords) {
        List<Message<K, V>> messages = new ArrayList<>(messageRecords.count());
        for (ConsumerRecord<K, V> messageRecord : messageRecords) {
            messages.add(convertMessage(messageRecord));
        }

        try {
            List<Message<K, V>> resultMessages = messageHandler.apply(messages);
            for (Message<K, V> resultMessage : resultMessages) {
                kafkaMessageProducer.produce(produceTopic, resultMessage);
            }
        } catch (Exception e) {
            log.error("An error occurred while processing the message. cause : {}", e.getMessage());
            deadLetterHandler.handle(messages, e);
        }
    }

    private Message<K, V> convertMessage(ConsumerRecord<K, V> messageRecord) {
        K key = messageRecord.key();
        V value = messageRecord.value();

        if (key instanceof DynamicMessage dynamicMessage) {
            key = KafkaDynamicMessageConverter.handleDynamicMessage(dynamicMessage);
        }

        if(value instanceof DynamicMessage dynamicMessage){
            value = KafkaDynamicMessageConverter.handleDynamicMessage(dynamicMessage);
        }

        return KafkaMessage.of(key, value, messageRecord);
    }
}
