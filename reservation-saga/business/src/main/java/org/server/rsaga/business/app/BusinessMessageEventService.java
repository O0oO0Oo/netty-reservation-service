package org.server.rsaga.business.app;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.business.domain.Business;
import org.server.rsaga.business.infra.repository.BusinessCustomRepository;
import org.server.rsaga.business.infra.repository.BusinessJpaRepository;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.messaging.message.ErrorDetails;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessMessageEventService {
    private final BusinessCustomRepository businessCustomRepository;
    private final BusinessJpaRepository businessJpaRepository;

    /**
     * @param message {@link org.server.rsaga.business.VerifyBusinessRequestOuterClass.VerifyBusinessRequest} 에 대한 응답.
     */
    @Transactional(readOnly = true)
    public Message<String, CreateReservationEvent> consumeSingleVerifyBusinessEvent(Message<String, CreateReservationEvent> message) {
        CreateReservationEvent requestPayload = message.payload();
        long businessId = requestPayload.getVerifyBusiness().getBusinessId();

        Business business = businessCustomRepository.findByIdOrElseThrow(businessId);
        Long verifiedBusinessId = business.getId();

        String key = String.valueOf(businessId);
        CreateReservationEvent responsePayload = CreateReservationEventBuilder
                .builder()
                .setVerifyBusinessResponse(verifiedBusinessId)
                .build();

        return SagaMessage.of(key, responsePayload, message.metadata(), Message.Status.RESPONSE_SUCCESS);
    }


    /**
     * @param messages {@link org.server.rsaga.business.VerifyBusinessRequestOuterClass.VerifyBusinessRequest} 의 bulk 처리, 응답.
     */
    @Transactional
    public List<Message<String, CreateReservationEvent>> consumeBulkVerifyBusinessEvents(List<Message<String, CreateReservationEvent>> messages) {
        Set<Long> businessIds = extractBusinessIdsFromMessages(messages);
        Set<Long> existingBusinessIds = findExistingBusinessIds(businessIds);

        return generateResponseMessages(messages, existingBusinessIds);
    }

    /**
     * 메시지에서 businessIds 추출
     */
    private Set<Long> extractBusinessIdsFromMessages(List<Message<String, CreateReservationEvent>> messages) {
        Set<Long> businessIds = new HashSet<>(messages.size());

        for (Message<String, CreateReservationEvent> message : messages) {
            CreateReservationEvent requestPayload = message.payload();
            long businessId = requestPayload.getVerifyBusiness().getBusinessId();
            businessIds.add(businessId);
        }
        return businessIds;
    }

    /**
     *  businessIds 로 한번에 조회
     */
    private Set<Long> findExistingBusinessIds(Set<Long> businessIds) {
        return businessJpaRepository.findAllById(businessIds).stream()
                .map(Business::getId)
                .collect(Collectors.toSet());
    }

    /**
     * 응답 메시지 생성
     */
    private List<Message<String, CreateReservationEvent>> generateResponseMessages(
            List<Message<String, CreateReservationEvent>> messages,
            Set<Long> existingBusinessIds) {
        List<Message<String, CreateReservationEvent>> responses = new ArrayList<>(messages.size());

        for (Message<String, CreateReservationEvent> message : messages) {
            String key = message.key();
            Map<String, byte[]> metadata = message.metadata();

            if (existingBusinessIds.contains(Long.parseLong(key))) {
                // 있는 businessId 라면 SUCCESS 로 응답.
                long verifiedBusinessId = Long.parseLong(key);
                CreateReservationEvent responsePayload = CreateReservationEventBuilder
                        .builder()
                        .setVerifyBusinessResponse(verifiedBusinessId)
                        .build();

                responses.add(
                        SagaMessage.of(key, responsePayload, metadata, Message.Status.RESPONSE_SUCCESS)
                );
            } else {
                // 없다면 FAILED 응답
                ErrorCode businessNotFound = ErrorCode.BUSINESS_NOT_FOUND;
                metadata.put(ErrorDetails.ERROR_CODE, businessNotFound.getCode().getBytes());
                metadata.put(ErrorDetails.ERROR_MESSAGE, businessNotFound.getMessage().getBytes());
                responses.add(
                        SagaMessage.of(key, message.payload(), metadata, Message.Status.RESPONSE_FAILED)
                );
            }
        }
        return responses;
    }
}