package org.server.rsaga.user.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.user.domain.User;
import org.server.rsaga.user.infra.repository.UserCustomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserMessageEventService {
    private final UserCustomRepository userCustomRepository;

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
}
