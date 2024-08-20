package org.server.rsaga.messaging.adapter.processor.impl;

import com.google.protobuf.DynamicMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.server.rsaga.messaging.adapter.message.KafkaMessage;
import org.server.rsaga.messaging.adapter.processor.KafkaMessageProcessor;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.adapter.util.KafkaDynamicMessageConverter;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.util.DeadLetterHandler;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

/**
 * todo : 스레드 EventLoop 로 변환해서 성능 측정해보기
 * <pre>
 * 컨슈머가 파티션 수 만큼 등록되어 이벤트를 소비한다.
 * registerHandler 에 등록된 메서드는 이벤트를 소비하여 처리한 후 같은 타입으로 반환을 하면,
 * 등록된 Producer 를 통해 이벤트를 발행하게 된다.
 *
 * 이벤트 처리중 실패하게 되면,
 * {@link DeadLetterHandler} 를 통해 실패한 이벤트가 예외 정보를 담아 발행된다.
 * </pre>
 * @param <K> 키
 * @param <V> 값
 */
@Slf4j
public class MultiThreadedKafkaMessageProcessor<K, V> implements KafkaMessageProcessor<K, V> {
    private final List<KafkaConsumer<K, V>> kafkaConsumers;
    private final KafkaMessageProducer<K, V> kafkaMessageProducer;
    private UnaryOperator<Message<K, V>> messageHandler;
    private final DeadLetterHandler<K, V> deadLetterHandler;
    private final ExecutorService threadPool;
    private final String produceTopic;

    public MultiThreadedKafkaMessageProcessor(List<KafkaConsumer<K, V>> kafkaConsumers,
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
    public void registerHandler(UnaryOperator<Message<K, V>> messageHandler) {
        this.messageHandler = messageHandler;
        for (KafkaConsumer<K, V> kafkaConsumer : kafkaConsumers) {
            threadPool.submit(() -> processMessages(kafkaConsumer));
        }
    }

    private void processMessages(KafkaConsumer<K, V> kafkaConsumer) {
        while (true) {
            try {
                ConsumerRecords<K, V> messageRecords = kafkaConsumer.poll(Duration.ofMillis(100));
                if (messageHandler != null && !messageRecords.isEmpty()) {
                    messageRecords.forEach(this::processRecord);
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

    private void processRecord(ConsumerRecord<K, V> messageRecord) {
        Message<K, V> message = convertMessage(messageRecord);

        try {
            log.debug("Received message: key={}, payload={}", message.key(), message.payload());
            Message<K, V> returnMessage = messageHandler.apply(message);
            kafkaMessageProducer.produce(produceTopic, returnMessage);
        } catch (Exception e) {
            log.error("An error occurred while processing the message. cause : {}", e.getMessage());
            deadLetterHandler.handle(message, e);
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