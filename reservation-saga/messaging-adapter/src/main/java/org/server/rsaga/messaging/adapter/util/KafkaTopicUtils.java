package org.server.rsaga.messaging.adapter.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.server.rsaga.messaging.adapter.execption.KafkaPartitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class KafkaTopicUtils {
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    public CompletableFuture<Void> createTopic(String topicName, int partitions, short replicationFactor) {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // todo : 토픽 생성 옵션 설정
        CreateTopicsOptions createTopicsOptions = new CreateTopicsOptions();

        try (Admin admin = Admin.create(properties)) {
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);

            CreateTopicsResult topicResult = admin.createTopics(Collections.singletonList(newTopic), createTopicsOptions);

            KafkaFuture<Void> kafkaFuture = topicResult
                    .values()
                    .get(topicName);

            return kafkaFuture
                    .toCompletionStage()
                    .toCompletableFuture()
                    .thenAccept(f -> log.info("Topic {} has been created.", topicName))
                    .exceptionally(exception ->
                            {
                                log.error("An error occurred in topic creation. cause : {}", exception.getMessage());
                                return null;
                            }
                    );
        } catch (Exception e) {
            log.error("Failed to create admin client. Cause: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * @param topicName 토픽 이름
     * @return topicName 의 파티션 수 정보
     */
    public int getPartitionCount(String topicName) {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(config)) {
            Map<String, TopicDescription> topicDescriptionMap = adminClient.describeTopics(Collections.singleton(topicName)).allTopicNames().get();
            TopicDescription topicDescription = topicDescriptionMap.get(topicName);
            return topicDescription.partitions().size();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaPartitionException("Loading topic partition information failed.");
        }
    }
}
