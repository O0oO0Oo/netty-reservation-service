package org.server.rsaga.reservation.infra.event.topic;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.messaging.MessagingTopics;
import org.server.rsaga.messaging.adapter.util.KafkaTopicUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationCreateSagaTopic {
    private final KafkaTopicUtils topicUtils;

    @PostConstruct
    public void createTopic() {
        // 유저 검증
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_VERIFY_USER.name(), 3, (short) 3);

        // 회사 검증
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_VERIFY_BUSINESS.name(), 3, (short) 3);

        // 상품 검증
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_VERIFY_RESERVABLEITEM.name(), 3, (short) 3);

        // 예약 횟수 초과 검증 및 PENDING 상태 예약 생성
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_CHECK_RESERVATION_LIMIT.name(), 3, (short) 3);

        // 결제
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_PAYMENT.name(), 3, (short) 3);

        // 상품 재고 차감
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_UPDATE_RESERVABLEITEM_QUANTITY.name(), 3, (short) 3);

        // 에약 생성 최종, PENDING 상태의 예약 RESERVED 로 상태 변경
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_FINAL_STEP.name(), 3, (short) 3);

        // 모든 서비스로부터의 응답
        topicUtils.createTopic(MessagingTopics.CREATE_RESERVATION_RESPONSE.name(), 3, (short) 3);
    }
}