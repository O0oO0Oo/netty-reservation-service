package org.server.rsaga.reservation.app.saga;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.business.VerifyBusinessRequestOuterClass;
import org.server.rsaga.common.messaging.MessagingTopics;
import org.server.rsaga.messaging.adapter.consumer.KafkaMessageConsumer;
import org.server.rsaga.messaging.adapter.producer.KafkaMessageProducer;
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

@Configuration
@RequiredArgsConstructor
public class CreateReservationSagaConfig {

    /**
     * <pre>
     * key : 이벤트의 키 값
     *
     * payload : 이벤트의 내용
     * {@link org.server.rsaga.reservation.CreateReservationEvent}
     * </pre>
     */
    @Bean("createReservationEventSagaDefinition")
    public SagaDefinition createReservationEventSagaDefinition(
            @Qualifier("userVerifyProducer")
            KafkaMessageProducer<String, CreateReservationEvent> userVerifyProducer,
            @Qualifier("businessVerifyProducer")
            KafkaMessageProducer<String, CreateReservationEvent> businessVerifyProducer,
            @Qualifier("reservableItemVerifyProducer")
            KafkaMessageProducer<String, CreateReservationEvent> reservableItemVerifyProducer,
            @Qualifier("checkReservationLimitProducer")
            KafkaMessageProducer<String, CreateReservationEvent> checkReservationLimitProducer,
            @Qualifier("paymentProducer")
            KafkaMessageProducer<String, CreateReservationEvent> paymentProducer,
            @Qualifier("updateItemQuantityProducer")
            KafkaMessageProducer<String, CreateReservationEvent> updateItemQuantityProducer,
            @Qualifier("createReservationFinalProducer")
            KafkaMessageProducer<String, CreateReservationEvent> createReservationFinalProducer
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
                        }, userVerifyProducer, StepType.EXECUTE
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
                        }, businessVerifyProducer, StepType.EXECUTE
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
                        }, reservableItemVerifyProducer, StepType.EXECUTE
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
                        }, checkReservationLimitProducer, StepType.EXECUTE
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
                        }, checkReservationLimitProducer, StepType.COMPENSATE
                        , "verifyUser", "verifyBusiness", "verifyItem", SagaDefinitionFactoryImpl.getInitialStepName()
                )


                /**
                 * ---------- 결제 ----------
                 * key
                 * userId
                 *
                 * payload
                 * {@link org.server.rsaga.payment.PaymentRequestOuterClass.PaymentRequest}
                 *
                 * requestQuantity (구매 요청 수) * price(가격) 만큼 유저의 잔액을 감소시킨다.
                 */
                .addStep("payment", MessagingTopics.CREATE_RESERVATION_PAYMENT.name(),
                        (messages) ->
                        {
                            // payload
                            String key = null;

                            // payload
                            long userId = 0;
                            long price = 0;
                            long requestQuantity = 0;
                            long reservationId = 0;
                            String paymentType = null;

                            for (SagaMessage<String, CreateReservationEvent> message : messages) {
                                CreateReservationEvent beforePayload = message.payload();

                                if (beforePayload.hasCheckedReservation()) {
                                    reservationId = beforePayload.getCheckedReservation().getReservationId();
                                }

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
                                    paymentType = beforePayload.getCreateReservationInit().getPaymentType();
                                }
                            }

                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    // 차감이므로 -
                                    .setPaymentRequest(
                                            paymentType, userId, reservationId, -(price * requestQuantity)
                                    )
                                    .build();

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, paymentProducer, StepType.EXECUTE
                        , "verifyUser", "verifyItem", "checkReservationLimit", SagaDefinitionFactoryImpl.getInitialStepName()
                )
                /**
                 * [Compensating Transaction]
                 * ---------- 결제 취소 ----------
                 * key
                 * userId
                 *
                 * payload
                 * {@link org.server.rsaga.payment.PaymentRequestOuterClass.PaymentRequest}
                 *
                 * requestQuantity (구매 요청 수) * price(가격) 만큼 유저의 잔액을 복구시킨다.
                 */
                .addStep("payment", MessagingTopics.CREATE_RESERVATION_PAYMENT.name(),
                        (messages) ->
                        {
                            String key = null;

                            // payload
                            long userId = 0;
                            long price = 0;
                            long requestQuantity = 0;
                            long reservationId = 0;
                            String paymentType = null; // 결제 타입

                            for (SagaMessage<String, CreateReservationEvent> message : messages) {
                                CreateReservationEvent beforePayload = message.payload();

                                if (beforePayload.hasCheckedReservation()) {
                                    reservationId = beforePayload.getCheckedReservation().getReservationId();
                                }

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
                                    paymentType = beforePayload.getCreateReservationInit().getPaymentType();
                                }
                            }

                            CreateReservationEvent payload = CreateReservationEventBuilder
                                    .builder()
                                    // 복구이므로 +
                                    .setPaymentRequest(
                                            paymentType, userId, reservationId, (price * requestQuantity)
                                    )
                                    .build();

                            return SagaMessage.of(key, payload, Message.Status.REQUEST);
                        }, paymentProducer, StepType.COMPENSATE
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
                        }, updateItemQuantityProducer, StepType.EXECUTE
                        , "verifyItem", "payment", SagaDefinitionFactoryImpl.getInitialStepName()
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
                        }, updateItemQuantityProducer, StepType.COMPENSATE
                        , "verifyItem", "payment", SagaDefinitionFactoryImpl.getInitialStepName()
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
                        }, createReservationFinalProducer, StepType.EXECUTE
                        , "checkReservationLimit", "updateItemQuantity"
                )
                .getSagaDefinition();
    }

    @Bean("createReservationEventSagaCoordinator")
    public SagaCoordinator<String, CreateReservationEvent> createReservationEventSagaCoordinator(
            @Qualifier("createReservationEventSagaDefinition")
            SagaDefinition sagaDefinition,
            @Qualifier("createReservationCoordinatorConsumer")
            KafkaMessageConsumer<String, CreateReservationEvent> messageConsumer
    ) {
        SagaStateComponentFactory<String, CreateReservationEvent> sagaStateComponentFactory = new InMemorySagaStateComponentFactory<>();
        SagaStateManager<String, CreateReservationEvent> sagaStateManger = sagaStateComponentFactory.createSagaStateManger();

        return new DefaultSagaCoordinator<>(sagaDefinition, sagaStateManger, messageConsumer);
    }
}