package org.server.rsaga.reservation.app.saga;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.business.VerifyBusinessRequestOuterClass;
import org.server.rsaga.common.messaging.MessagingTopics;
import org.server.rsaga.messaging.adapter.consumer.KafkaMessageConsumer;
import org.server.rsaga.messaging.adapter.consumer.factory.KafkaMessageConsumerFactory;
import org.server.rsaga.messaging.adapter.consumer.strategy.KafkaMessageConsumerType;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
import org.server.rsaga.messaging.adapter.producer.factory.KafkaMessageProducerFactory;
import org.server.rsaga.messaging.adapter.producer.strategy.KafkaMessageProducerType;
import org.server.rsaga.messaging.adapter.util.KafkaTopicUtils;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservableitem.VerifyReservableItemRequestOuterClass;
import org.server.rsaga.reservation.CheckReservationLimitRequestOuterClass;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.reservation.ReservationStatusOuterClass;
import org.server.rsaga.saga.api.SagaCoordinator;
import org.server.rsaga.saga.api.SagaDefinition;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.saga.api.factory.SagaDefinitionFactoryImpl;
import org.server.rsaga.saga.api.impl.DefaultSagaCoordinator;
import org.server.rsaga.saga.state.SagaStateManager;
import org.server.rsaga.saga.state.factory.InMemorySagaStateComponentFactory;
import org.server.rsaga.saga.state.factory.SagaStateComponentFactory;
import org.server.rsaga.saga.step.impl.StepType;
import org.server.rsaga.user.VerifyUserRequestOuterClass;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class CreateReservationSagaConfig {
    private final KafkaTopicUtils topicUtils;
    private final KafkaMessageProducerFactory kafkaMessageProducerFactory;

    @PostConstruct
    public void createTopic() {
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_VERIFY_USER.name(), 3, (short) 3);
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_VERIFY_BUSINESS.name(), 3, (short) 3);
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_VERIFY_RESERVABLEITEM.name(), 3, (short) 3);
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_CHECK_RESERVATION_LIMIT.name(), 3, (short) 3);
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_UPDATE_USER_BALANCE.name(), 3, (short) 3);
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_UPDATE_RESERVABLEITEM_QUANTITY.name(), 3, (short) 3);
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_FINAL_STEP.name(), 3, (short) 3);

        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_RESPONSE.name(), 3, (short) 3);
    }

    /**
     * @return Exactly-Once {@link KafkaMessageProducer}
     */
    @Bean("ExactlyOnceKafkaMessageProducer")
    public KafkaMessageProducer<String, CreateReservationEvent> messageProducer() {
        Properties config = new Properties();
        config.put("transactional.id", "create-reservation-transaction");

        return kafkaMessageProducerFactory.createProducer(config, KafkaMessageProducerType.EXACTLY_ONCE);
    }

    /**
     * todo 토픽별로 프로듀서를 사용할지
     * <pre>
     * key : 이벤트의 키 값
     *
     * payload : 이벤트의 내용
     * {@link org.server.rsaga.reservation.CreateReservationEvent}
     * </pre>
     */
    @Bean("createReservationEventSagaDefinition")
    public SagaDefinition createReservationEventSagaDefinition(
            @Qualifier("ExactlyOnceKafkaMessageProducer")
            KafkaMessageProducer<String, CreateReservationEvent> messageProducer
    ) {
        SagaDefinitionFactoryImpl<String, CreateReservationEvent> sagaDefinitionFactory = new SagaDefinitionFactoryImpl<>();

        return sagaDefinitionFactory
                /**
                 * ---------- user id 검증 ----------
                 * key
                 * userId
                 *
                 *  payload
                 * {@link VerifyUserRequestOuterClass.VerifyUserRequest}
                 * 1. userId
                 *
                 */
                .addStep("verifyUser", MessagingTopics.CREATE_RESERVATION_VERIFY_USER.name(),
                        (msg) ->
                        {
                            // payload
                            CreateReservationEvent beforePayload = msg.payload();
                            long userId = beforePayload.getCreateReservationInit().getUserId();

                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    .setVerifyUserRequest(userId)
                                    .build();

                            // key
                            String key = String.valueOf(userId);

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, messageProducer, StepType.EXECUTE
                )


                /**
                 * ---------- business id 검증 ----------
                 * key
                 * businessId
                 *
                 * payload
                 * {@link VerifyBusinessRequestOuterClass.VerifyBusinessRequest}
                 */
                .addStep("verifyBusiness", MessagingTopics.CREATE_RESERVATION_VERIFY_BUSINESS.name(),
                        (msg) ->
                        {
                            // payload - businessId
                            CreateReservationEvent beforePayload = msg.payload();
                            long businessId = beforePayload.getCreateReservationInit().getBusinessId();

                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    .setVerifyBusinessRequest(businessId)
                                    .build();

                            // key
                            String key = String.valueOf(businessId);

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, messageProducer, StepType.EXECUTE
                )


                /**
                 * ---------- reservableitem id, 구매 요청 수가 인당 구매 제한을 넘는지 검증 ----------
                 * key
                 * reservableItemId
                 *
                 * payload
                 * {@link VerifyReservableItemRequestOuterClass.VerifyReservableItemRequest}
                 */
                .addStep("verifyItem", MessagingTopics.CREATE_RESERVATION_VERIFY_RESERVABLEITEM.name(),
                        (msg) -> {
                            // payload
                            CreateReservationEvent beforePayload = msg.payload();
                            long reservableItemId = beforePayload.getCreateReservationInit().getReservableItemId();
                            long reservableItemTimeId = beforePayload.getCreateReservationInit().getReservableItemTimeId();
                            long businessId = beforePayload.getCreateReservationInit().getBusinessId();
                            long reservationQuantity = beforePayload.getCreateReservationInit().getRequestQuantity();

                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    .setVerifyReservableItemRequest(
                                            reservableItemId,
                                            reservableItemTimeId,
                                            businessId,
                                            reservationQuantity)
                                    .build();

                            // key
                            String key = String.valueOf(reservableItemId);

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, messageProducer, StepType.EXECUTE
                )


                /**
                 * ---------- 인당 구매제한 검사, pending 상태의 pending 상태의 예약 생성 ----------
                 * todo : 인덱스를 정한 후에 키도 변경해야 함.
                 * key
                 * userId
                 *
                 * payload
                 * {@link CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest}
                 */
                .addStep("checkReservationLimit", MessagingTopics.CREATE_RESERVATION_CHECK_RESERVATION_LIMIT.name(),
                        (messages) ->
                        {
                            // payload, key, metadata
                            String key = null;

                            long userId = 0;
                            long reservableItemId = 0;
                            long reservableTimeId = 0;
                            long businessId = 0;
                            long reservationId = 0;
                            long maxQuantityPerUser = 0;
                            long requestQuantity = 0;

                            for (SagaMessage<String, CreateReservationEvent> msg : messages) {
                                CreateReservationEvent beforePayload = msg.payload();

                                // 검증된 user 정보 : userId
                                if (beforePayload.hasVerifiedUser()) {
                                    userId = beforePayload.getVerifiedUser().getUserId();

                                    // key 설정 : userId 별로 파티션 찾아가도록
                                    key = String.valueOf(userId);
                                }

                                // 검증된 item 정보 : reservableItemId, maxQuantityPerUser
                                if (beforePayload.hasVerifiedReservableItem()) {
                                    reservableItemId = beforePayload.getVerifiedReservableItem().getReservableItemId();
                                    reservableTimeId = beforePayload.getVerifiedReservableItem().getReservableTimeId();
                                    maxQuantityPerUser = beforePayload.getVerifiedReservableItem().getMaxQuantityPerUser();
                                }

                                // 사가 시작 요청 정보 : 예약 원하는 갯수
                                if (beforePayload.hasCreateReservationInit()) {
                                    requestQuantity = beforePayload.getCreateReservationInit().getRequestQuantity();
                                    reservationId = msg.correlationId().toLong();
                                }

                                if(beforePayload.hasVerifiedBusiness()){
                                    businessId = beforePayload.getVerifiedBusiness().getBusinessId();
                                }
                            }

                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    .setCheckReservationLimitRequest(
                                            userId, reservableItemId, reservableTimeId, businessId, reservationId, maxQuantityPerUser, requestQuantity,
                                            ReservationStatusOuterClass.ReservationStatus.PENDING
                                    )
                                    .build();

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, messageProducer, StepType.EXECUTE
                        , "verifyUser", "verifyBusiness", "verifyItem", SagaDefinitionFactoryImpl.getInitialStepName()
                )
                /**
                 * [Compensating Transaction]
                 * ---------- 인당 구매제한 검사, pending 상태의 pending 상태의 예약 생성, FAILED 로 설정 ----------
                 * key
                 * userId
                 *
                 * payload
                 * {@link CheckReservationLimitRequestOuterClass.CheckReservationLimitRequest}
                 */
                .addStep("checkReservationLimit", MessagingTopics.CREATE_RESERVATION_CHECK_RESERVATION_LIMIT.name(),
                        (messages) ->
                        {
                            // payload, key, metadata
                            String key = null;

                            long userId = 0;
                            long reservableItemId = 0;
                            long reservableTimeId = 0;
                            long businessId = 0;
                            long reservationId = 0;
                            long maxQuantityPerUser = 0;
                            long requestQuantity = 0;

                            for (SagaMessage<String, CreateReservationEvent> msg : messages) {
                                CreateReservationEvent beforePayload = msg.payload();

                                // 검증된 user 정보 : userId
                                if (beforePayload.hasVerifiedUser()) {
                                    userId = beforePayload.getVerifiedUser().getUserId();

                                    // key 설정 : userId 별로 파티션 찾아가도록
                                    key = String.valueOf(userId);
                                }

                                // 검증된 item 정보 : reservableItemId, maxQuantityPerUser
                                if (beforePayload.hasVerifiedReservableItem()) {
                                    reservableItemId = beforePayload.getVerifiedReservableItem().getReservableItemId();
                                    reservableTimeId = beforePayload.getVerifiedReservableItem().getReservableTimeId();
                                    maxQuantityPerUser = beforePayload.getVerifiedReservableItem().getMaxQuantityPerUser();
                                }

                                // 사가 시작 요청 정보 : 예약 원하는 갯수
                                if (beforePayload.hasCreateReservationInit()) {
                                    requestQuantity = beforePayload.getCreateReservationInit().getRequestQuantity();
                                    reservationId = msg.correlationId().toLong();
                                }

                                if(beforePayload.hasVerifiedBusiness()){
                                    businessId = beforePayload.getVerifiedBusiness().getBusinessId();
                                }
                            }

                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    .setCheckReservationLimitRequest(
                                            userId, reservableItemId, reservableTimeId, businessId, reservationId, maxQuantityPerUser, requestQuantity,
                                            ReservationStatusOuterClass.ReservationStatus.FAILED
                                    )
                                    .build();

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, messageProducer, StepType.COMPENSATE
                        , "verifyUser", "verifyBusiness", "verifyItem", SagaDefinitionFactoryImpl.getInitialStepName()
                )


                /**
                 * ---------- 유저 잔액 감소 ----------
                 * key
                 * userId
                 *
                 * payload
                 * {@link org.server.rsaga.user.UpdateUserBalanceRequestOuterClass.UpdateUserBalanceRequest}
                 *
                 * requestQuantity (구매 요청 수) * price(가격) 만큼 유저의 잔액을 감소시킨다.
                 */
                .addStep("updateUserBalance", MessagingTopics.CREATE_RESERVATION_UPDATE_USER_BALANCE.name(),
                        (messages) ->
                        {
                            // payload, key
                            String key = null;

                            long userId = 0;
                            long price = 0;
                            long requestQuantity = 0;

                            for (SagaMessage<String, CreateReservationEvent> message : messages) {
                                CreateReservationEvent beforePayload = message.payload();

                                if (beforePayload.hasVerifiedUser()) {
                                    userId = beforePayload.getVerifiedUser().getUserId();

                                    // key
                                    key = String.valueOf(userId);
                                }

                                if (beforePayload.hasVerifiedReservableItem()) {
                                    price = beforePayload.getVerifiedReservableItem().getReservableItemPrice();
                                }

                                if (beforePayload.hasCreateReservationInit()) {
                                    requestQuantity = beforePayload.getCreateReservationInit().getRequestQuantity();
                                }
                            }

                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    // 차감이므로 -
                                    .setUpdateUserBalanceRequest(
                                            userId, -(price * requestQuantity)
                                    )
                                    .build();

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, messageProducer, StepType.EXECUTE
                        , "verifyUser", "verifyItem", "checkReservationLimit", SagaDefinitionFactoryImpl.getInitialStepName()
                )
                /**
                 * [Compensating Transaction]
                 * ---------- 유저 잔액 감소 ----------
                 * key
                 * userId
                 *
                 * payload
                 * {@link org.server.rsaga.user.UpdateUserBalanceRequestOuterClass.UpdateUserBalanceRequest}
                 *
                 * requestQuantity (구매 요청 수) * price(가격) 만큼 유저의 잔액을 복구시킨다.
                 */
                .addStep("updateUserBalance", MessagingTopics.CREATE_RESERVATION_UPDATE_USER_BALANCE.name(),
                        (messages) ->
                        {
                            // payload, key, metadata
                            String key = null;

                            long userId = 0;
                            long price = 0;
                            long requestQuantity = 0;

                            for (SagaMessage<String, CreateReservationEvent> message : messages) {
                                CreateReservationEvent beforePayload = message.payload();

                                if (beforePayload.hasVerifiedUser()) {
                                    userId = beforePayload.getVerifiedUser().getUserId();

                                    // key
                                    key = String.valueOf(userId);
                                }

                                if (beforePayload.hasVerifiedReservableItem()) {
                                    price = beforePayload.getVerifiedReservableItem().getReservableItemPrice();
                                }

                                if (beforePayload.hasCreateReservationInit()) {
                                    requestQuantity = beforePayload.getCreateReservationInit().getRequestQuantity();
                                }
                            }

                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    // 복구이므로 +
                                    .setUpdateUserBalanceRequest(
                                            userId, price * requestQuantity
                                    )
                                    .build();

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, messageProducer, StepType.COMPENSATE
                        , "verifyUser", "verifyItem", "checkReservationLimit", SagaDefinitionFactoryImpl.getInitialStepName()
                )


                /**
                 * ---------- 아이템 재고 감소 ----------
                 * key
                 * reservableItemId
                 *
                 * payload
                 * {@link org.server.rsaga.reservableitem.UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest}
                 *
                 * requestQuantity (구매 요청 수) 만큼 아이템의 재고를 감소시킨다.
                 */
                .addStep("updateItemQuantity", MessagingTopics.CREATE_RESERVATION_UPDATE_RESERVABLEITEM_QUANTITY.name(),
                        (messages) ->
                        {
                            String key = null;

                            long reservableItemId = 0;
                            long reservableTimeId = 0;
                            long requestQuantity = 0;

                            for (SagaMessage<String, CreateReservationEvent> message : messages) {
                                CreateReservationEvent beforePayload = message.payload();
                                if (beforePayload.hasVerifiedReservableItem()) {
                                    reservableItemId = beforePayload.getVerifiedReservableItem().getReservableItemId();
                                    reservableTimeId = beforePayload.getVerifiedReservableItem().getReservableTimeId();

                                    // key
                                    key = String.valueOf(reservableTimeId);
                                }

                                if (beforePayload.hasCreateReservationInit()) {
                                    requestQuantity = beforePayload.getCreateReservationInit().getRequestQuantity();
                                }
                            }

                            // payload
                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    .setUpdateReservableItemQuantityRequest(reservableItemId, reservableTimeId, -requestQuantity)
                                    .build();

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, messageProducer, StepType.EXECUTE
                        , "verifyItem", "updateUserBalance", SagaDefinitionFactoryImpl.getInitialStepName()
                )
                /**
                 * [Compensating Transaction]
                 * ---------- 아이템 재고 감소 ----------
                 * key
                 * reservableItemId
                 *
                 * payload
                 * {@link org.server.rsaga.reservableitem.UpdateReservableItemQuantityRequestOuterClass.UpdateReservableItemQuantityRequest}
                 *
                 * requestQuantity (구매 요청 수) 만큼 아이템의 재고를 복구시킨다.
                 */
                .addStep("updateItemQuantity", MessagingTopics.CREATE_RESERVATION_UPDATE_RESERVABLEITEM_QUANTITY.name(),
                        (messages) ->
                        {
                            String key = null;

                            long reservableItemId = 0;
                            long reservableTimeId = 0;
                            long requestQuantity = 0;

                            for (SagaMessage<String, CreateReservationEvent> message : messages) {
                                CreateReservationEvent beforePayload = message.payload();
                                if (beforePayload.hasVerifiedReservableItem()) {
                                    reservableItemId = beforePayload.getVerifiedReservableItem().getReservableItemId();
                                    reservableTimeId = beforePayload.getVerifiedReservableItem().getReservableTimeId();

                                    // key
                                    key = String.valueOf(reservableItemId);
                                }

                                if (beforePayload.hasCreateReservationInit()) {
                                    requestQuantity = beforePayload.getCreateReservationInit().getRequestQuantity();
                                }
                            }

                            // payload
                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    .setUpdateReservableItemQuantityRequest(reservableItemId, reservableTimeId, requestQuantity)
                                    .build();

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, messageProducer, StepType.COMPENSATE
                        , "verifyItem", "updateUserBalance", SagaDefinitionFactoryImpl.getInitialStepName()
                )


                /**
                 * ---------- 마지막 단계 ----------
                 * key
                 * reservationId
                 * 
                 * payload
                 * {@link org.server.rsaga.reservation.CreateReservationFinalRequestOuterClass.CreateReservationFinalRequest}
                 * 
                 * pending 상태의 reservation 을 reserved 로 변경
                 */
                .addStep("createReservationFinal", MessagingTopics.CREATE_RESERVATION_FINAL_STEP.name(),
                        (messages) ->
                        {
                            String key = null;
                            long reservationId = 0;

                            for (SagaMessage<String, CreateReservationEvent> message : messages) {
                                CreateReservationEvent beforePayload = message.payload();

                                if (beforePayload.hasCheckedReservation()) {
                                    reservationId = beforePayload.getCheckedReservation().getReservationId();

                                    // key
                                    key = String.valueOf(reservationId);
                                }
                            }

                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    .setRegisterReservationFinalRequest(reservationId)
                                    .build();

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, messageProducer, StepType.EXECUTE
                        , "checkReservationLimit", "updateItemQuantity"
                )
                .getSagaDefinition();
    }

    @Bean("createReservationEventSagaCoordinator")
    public SagaCoordinator<String, CreateReservationEvent> createReservationEventSagaCoordinator(
            @Qualifier("createReservationEventSagaDefinition")
            SagaDefinition sagaDefinition,
            KafkaMessageConsumerFactory messageConsumerFactory
    ) {
        SagaStateComponentFactory<String, CreateReservationEvent> sagaStateComponentFactory = new InMemorySagaStateComponentFactory<>();
        SagaStateManager<String, CreateReservationEvent> sagaStateManger = sagaStateComponentFactory.createSagaStateManger();

        Properties config = new Properties();
        config.put("group.id", "create-reservation-group");
        config.put("consumer.topic", MessagingTopics.CREATE_RESERVATION_RESPONSE.name());
        config.put("specific.protobuf.value.type", CreateReservationEvent.class.getName());

        KafkaMessageConsumer<String, CreateReservationEvent> messageConsumer = messageConsumerFactory.createConsumer(
                config, KafkaMessageConsumerType.AT_LEAST_ONCE
        );
        return new DefaultSagaCoordinator<>(sagaDefinition, sagaStateManger, messageConsumer);
    }
}