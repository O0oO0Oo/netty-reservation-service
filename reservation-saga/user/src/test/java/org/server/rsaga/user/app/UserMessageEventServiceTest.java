package org.server.rsaga.user.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.saga.api.SagaMessage;
import org.server.rsaga.user.domain.User;
import org.server.rsaga.user.infra.repository.UserCustomRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserMessageEventService Tests")
class UserMessageEventServiceTest {
    @Mock
    UserCustomRepository userCustomRepository;

    @InjectMocks
    UserMessageEventService userMessageEventService;

    @Mock
    User user;

    long userId;
    Message<String, CreateReservationEvent> requestMessage;

    @BeforeEach
    void setUp() {
        userId = 1L;

        CreateReservationEvent requestPayload = CreateReservationEventBuilder
                .builder()
                .setVerifyUserRequest(userId)
                .build();

        requestMessage = SagaMessage.of("key", requestPayload, null, Message.Status.REQUEST);
    }

    @Test
    @DisplayName("consumerVerifyUserEvent() - success")
    void should_returnSuccessMessage_when_userExists() {
        // given
        when(userCustomRepository.findByIdOrElseThrow(userId)).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        // when
        Message<String, CreateReservationEvent> responseMessage = userMessageEventService.consumerVerifyUserEvent(requestMessage);

        // then
        assertNotNull(responseMessage);
        assertEquals(Message.Status.RESPONSE_SUCCESS, responseMessage.status());
        assertEquals(userId, responseMessage.payload().getVerifiedUser().getUserId());

        verify(userCustomRepository).findByIdOrElseThrow(userId);
    }
}