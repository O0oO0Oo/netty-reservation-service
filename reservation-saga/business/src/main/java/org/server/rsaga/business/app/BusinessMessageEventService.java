package org.server.rsaga.business.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.rsaga.business.domain.Business;
import org.server.rsaga.business.infra.repository.BusinessCustomRepository;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessMessageEventService {
    private final BusinessCustomRepository businessCustomRepository;

    /**
     * @param message {@link org.server.rsaga.business.VerifyBusinessRequestOuterClass.VerifyBusinessRequest} 에 대한 응답.
     */
    @Transactional(readOnly = true)
    public Message<String, CreateReservationEvent> consumeVerifyBusinessEvent(Message<String, CreateReservationEvent> message) {
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
}