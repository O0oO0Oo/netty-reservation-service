package org.server.rsaga.user.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.user.domain.User;
import org.server.rsaga.user.infra.repository.UserCustomRepository;
import org.server.rsaga.user.infra.repository.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserMessageEventService {
    private final UserCustomRepository userCustomRepository;
    private final UserJpaRepository userJpaRepository;

    /**
     * 단건 처리
     */
    @Transactional(readOnly = true)
    public Message<String, CreateReservationEvent> consumerVerifyUserEvent(Message<String, CreateReservationEvent> message) {
        CreateReservationEvent requestPayload = message.payload();
        long userId = requestPayload.getVerifyUser().getUserId();

        User user = userCustomRepository.findByIdOrElseThrow(userId);

        String key = String.valueOf(user.getId());
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setVerifyUserResponse(user.getId())
                .build();

        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }

    /**
     * 벌크 처리
     * {@link org.server.rsaga.user.VerifyUserRequestOuterClass.VerifyUserRequest} 요청을 받아
     * {@link org.server.rsaga.user.VerifyUserResponseOuterClass.VerifyUserResponse} 응답을 준다.
     */
    @Transactional(readOnly = true)
    public List<Message<String, CreateReservationEvent>> consumerBulkVerifyUserEvent(List<Message<String, CreateReservationEvent>> messages) {
        Set<Long> userIds = extractUserIdsFromMessages(messages);
        Set<Long> existingUserIds = findExistingUserIds(userIds);

        List<Message<String, CreateReservationEvent>> responses = generateResponseMessages(messages, existingUserIds);

        return responses;
    }

    private Set<Long> extractUserIdsFromMessages(List<Message<String, CreateReservationEvent>> messages) {
        Set<Long> userIds = new HashSet<>(messages.size());

        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            long userId = requestPayload.getVerifyUser().getUserId();
            userIds.add(userId);
        }
        return userIds;
    }

    private Set<Long> findExistingUserIds(Set<Long> userIds) {
        return userJpaRepository.findAllById(userIds).stream()
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    private List<Message<String, CreateReservationEvent>> generateResponseMessages(
            List<Message<String, CreateReservationEvent>> messages,
            Set<Long> existingUserIds) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        for (Message<String, CreateReservationEvent> message : messages) {
            String key = message.key();
            Map<String, byte[]> metadata = message.metadata();

            if (existingUserIds.contains(Long.parseLong(key))) {
                // 있는 userid 라면 SUCCESS 로 응답.
                long verifiedUserId = Long.parseLong(key);
                CreateReservationEvent responsePayload = CreateReservationEventBuilder
                        .builder()
                        .setVerifyUserResponse(verifiedUserId)
                        .build();

                responses.add(
                        SagaMessage.of(key, responsePayload, metadata, Message.Status.RESPONSE_SUCCESS)
                );
            } else {
                // 없다면 FAILED 응답
                responses.add(
                        SagaMessage.createFailureResponse(message, ErrorCode.USER_NOT_FOUND)
                );
            }
        }
        return responses;
    }
}