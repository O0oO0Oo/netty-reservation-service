package org.server.rsaga.reservation.app;

import io.hypersistence.tsid.TSID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.messaging.message.Message;
import org.server.rsaga.messaging.schema.reservation.CreateReservationEventBuilder;
import org.server.rsaga.reservation.CreateReservationEvent;
import org.server.rsaga.reservation.ReservationStatusOuterClass;
import org.server.rsaga.reservation.domain.Reservation;
import org.server.rsaga.reservation.domain.constant.ReservationStatus;
import org.server.rsaga.reservation.infra.repository.ReservationCustomRepository;
import org.server.rsaga.reservation.infra.repository.ReservationJpaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("ReservationMessageEventService tests")
@ExtendWith(MockitoExtension.class)
class ReservationMessageEventServiceTest {
    @Mock
    private ReservationJpaRepository reservationJpaRepository;
    @Mock
    private ReservationCustomRepository reservationCustomRepository;
    @Mock
    private Reservation reservation;

    @InjectMocks
    private ReservationMessageEventService reservationMessageEventService;

    @Test
    @DisplayName("consumeCheckReservationLimitEvent() - pending status - succeed")
    void should_succeed_when_pendingStatus() {
        // given
        long reservationId = TSID.fast().toLong();
        long reservableItemId = 1L;
        long reservableTimeId = 1L;
        long userId = 1L;
        long businessId = 1L;
        long maxQuantityPerUser = 10L;
        long requestQuantity = 5L;

        CreateReservationEvent requestPayload = CreateReservationEventBuilder.builder()
                .setCheckReservationLimitRequest(
                        userId,
                        reservableItemId,
                        reservableTimeId,
                        businessId,
                        reservationId,
                        maxQuantityPerUser,
                        requestQuantity,
                        ReservationStatusOuterClass.ReservationStatus.PENDING
                )
                .build();

        Message<String, CreateReservationEvent> message = mock(Message.class);
        when(message.payload()).thenReturn(requestPayload);

        when(reservationJpaRepository.findSumQuantityByUserIdAndReservableItemIdAndReserved(
                new ForeignKey(userId),
                new ForeignKey(reservableItemId)
        )).thenReturn(5);

        when(reservationJpaRepository.save(any(Reservation.class))).thenReturn(reservation);

        // when
        Message<String, CreateReservationEvent> response = reservationMessageEventService.consumeCheckReservationLimitEvent(message);

        // then
        verify(reservationJpaRepository, times(1)).findSumQuantityByUserIdAndReservableItemIdAndReserved(any(ForeignKey.class), any(ForeignKey.class));
        verify(reservationJpaRepository, times(1)).save(any(Reservation.class));
        assertEquals(Message.Status.RESPONSE_SUCCESS, response.status(), "Response status should be 'RESPONSE_SUCCESS");
    }

    @Test
    @DisplayName("consumeCheckReservationLimitEvent() - failed status - succeed")
    void should_succeed_when_failedStatus() {
        // given
        long reservationId = TSID.fast().toLong();
        long reservableItemId = 1L;
        long reservableTimeId = 1L;
        long userId = 1L;
        long businessId = 1L;
        long maxQuantityPerUser = 10L;
        long requestQuantity = 5L;

        CreateReservationEvent requestPayload = CreateReservationEventBuilder.builder()
                .setCheckReservationLimitRequest(
                        userId,
                        reservableItemId,
                        reservableTimeId,
                        businessId,
                        reservationId,
                        maxQuantityPerUser,
                        requestQuantity,
                        ReservationStatusOuterClass.ReservationStatus.FAILED
                )
                .build();


        Message<String, CreateReservationEvent> message = mock(Message.class);
        when(message.payload()).thenReturn(requestPayload);

        when(reservationCustomRepository.findReservationByIdOrElseThrow(reservationId)).thenReturn(reservation);

        // when
        Message<String, CreateReservationEvent> response = reservationMessageEventService.consumeCheckReservationLimitEvent(message);

        // then
        verify(reservationCustomRepository, only()).findReservationByIdOrElseThrow(reservationId);
        verify(reservation, only()).updateStatus(ReservationStatus.FAILED);
        assertEquals(Message.Status.RESPONSE_SUCCESS, response.status(), "Response status should be 'RESPONSE_SUCCESS");
    }

    @Test
    @DisplayName("consumeCheckReservationLimitEvent() - max quantity exceeded - throws CustomException")
    void should_throw_when_maxQuantityExceeded() {
        // given
        long reservationId = TSID.fast().toLong();
        long reservableItemId = 1L;
        long reservableTimeId = 1L;
        long userId = 1L;
        long businessId = 1L;
        long maxQuantityPerUser = 10L;
        long requestQuantity = 5L;

        CreateReservationEvent requestPayload = CreateReservationEventBuilder.builder()
                .setCheckReservationLimitRequest(
                        userId,
                        reservableItemId,
                        reservableTimeId,
                        businessId,
                        reservationId,
                        maxQuantityPerUser,
                        requestQuantity,
                        ReservationStatusOuterClass.ReservationStatus.PENDING
                )
                .build();


        Message<String, CreateReservationEvent> message = mock(Message.class);
        when(message.payload()).thenReturn(requestPayload);

        when(reservationJpaRepository.findSumQuantityByUserIdAndReservableItemIdAndReserved(
                new ForeignKey(userId),
                new ForeignKey(reservableItemId)
        )).thenReturn(6);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                reservationMessageEventService.consumeCheckReservationLimitEvent(message));

        // then
        assertEquals(ErrorCode.MAX_QUANTITY_EXCEEDED, exception.getErrorCode(), "Error code should be 'MAX_QUANTITY_EXCEEDED");
    }

    @Test
    @DisplayName("consumeCreateReservationFinalEvent() - success")
    void should_succeed_when_consumeCreateReservationFinalEvent() {
        // given
        long reservationId = TSID.fast().toLong();
        long reservableItemId = 1L;
        long reservableTimeId = 1L;
        long userId = 1L;
        long businessId = 1L;
        long requestQuantity = 5L;

        CreateReservationEvent requestPayload = CreateReservationEventBuilder.builder()
                .setRegisterReservationFinalRequest(
                        reservationId
                )
                .build();


        Message<String, CreateReservationEvent> message = mock(Message.class);
        when(message.payload()).thenReturn(requestPayload);

        when(reservationCustomRepository.findReservationByIdOrElseThrow(reservationId)).thenReturn(reservation);

        when(reservation.getId()).thenReturn(reservationId);
        when(reservation.getBusinessId()).thenReturn(businessId);
        when(reservation.getUserId()).thenReturn(userId);
        when(reservation.getReservableItemId()).thenReturn(reservableItemId);
        when(reservation.getReservableTimeId()).thenReturn(reservableTimeId);
        when(reservation.getQuantity()).thenReturn(requestQuantity);

        // when
        Message<String, CreateReservationEvent> response = reservationMessageEventService.consumeCreateReservationFinalEvent(message);

        // then
        verify(reservationCustomRepository, only()).findReservationByIdOrElseThrow(reservationId);
        verify(reservation, times(1)).updateStatus(ReservationStatus.RESERVED);
        assertEquals(Message.Status.RESPONSE_SUCCESS, response.status(), "Response status should be SUCCESS");
    }
}