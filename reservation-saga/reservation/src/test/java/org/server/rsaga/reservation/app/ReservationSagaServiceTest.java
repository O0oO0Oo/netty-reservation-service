package org.server.rsaga.reservation.app;

import io.hypersistence.tsid.TSID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.common.domain.constant.PaymentType;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.reservation.dto.request.CreateReservationRequest;
import org.server.rsaga.reservation.dto.response.ReservationDetailsResponse;
import org.server.rsaga.saga.api.SagaCoordinator;
import org.server.rsaga.saga.api.SagaMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@DisplayName("ReservationSagaService tests")
@ExtendWith(MockitoExtension.class)
class ReservationSagaServiceTest {
    @Mock
    private SagaCoordinator<String, CreateReservationEvent> sagaCoordinator;

    @InjectMocks
    private ReservationSagaService reservationSagaService;

    @Test
    @DisplayName("createReservation() - valid request - success")
    void should_succeed_when_createReservation() {
        // given
        TSID correlationId = TSID.fast();
        long userId = 1L;
        long businessId = 1L;
        long reservableItemId = 1L;
        long reservableTimeId = 1L;
        long requestQuantity = 5L;
        PaymentType paymentType = PaymentType.WALLET;

        CreateReservationRequest request = new CreateReservationRequest(
                userId,
                businessId,
                reservableItemId,
                reservableTimeId,
                requestQuantity,
                paymentType
        );

        SagaMessage<String, CreateReservationEvent> sagaResult = mock(SagaMessage.class);

        CreateReservationEvent createdReservationEvent = CreateReservationEventBuilder.builder()
                .setRegisterReservationFinalResponse(
                        correlationId.toLong(),
                        request.businessId(),
                        request.userId(),
                        request.reservableItemId(),
                        request.reservableTimeId(),
                        request.requestQuantity()
                )
                .build();

        when(sagaCoordinator.start(any())).thenReturn(sagaResult);
        when(sagaResult.payload()).thenReturn(createdReservationEvent);

        // when
        ReservationDetailsResponse response = reservationSagaService.createReservation(request);

        // then
        verify(sagaCoordinator).start(any(SagaMessage.class));
        assertNotNull(response);
        assertEquals(correlationId.toLong(), response.reservationId());
        assertEquals(1L, response.businessId());
        assertEquals(1L, response.userId());
        assertEquals(1L, response.reservableItemId());
        assertEquals(1L, response.reservableTimeId());
        assertEquals(5L, response.quantity());
    }
}