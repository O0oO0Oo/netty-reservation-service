package org.server.rsaga.reservation.app;

import io.hypersistence.tsid.TSID;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.common.domain.constant.PaymentType;
import org.server.rsaga.common.netty.SharedResponseEventExecutorGroup;
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
    @Mock
    private SharedResponseEventExecutorGroup sharedResponseEventExecutorGroup;

    @InjectMocks
    private ReservationSagaService reservationSagaService;

    @Test
    @DisplayName("createReservation() - valid request - success")
    void should_succeed_when_createReservation() {
        // given
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

        Promise<SagaMessage<String, CreateReservationEvent>> sagaResult = mock(Promise.class);

        when(sagaCoordinator.start(any())).thenReturn(sagaResult);

        // when
        Promise<ReservationDetailsResponse> reservation = reservationSagaService.createReservation(request);

        // then
        verify(sagaCoordinator).start(any(SagaMessage.class));
    }
}