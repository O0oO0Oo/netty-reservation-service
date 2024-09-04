package org.server.rsaga.reservableitem.app;

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
import org.server.rsaga.reservableitem.domain.ReservableItem;
import org.server.rsaga.reservableitem.infra.repository.ReservableItemCustomRepository;
import org.server.rsaga.reservation.CreateReservationEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessMessageEventService tests")
class ReservableItemMessageEventServiceTest {
    @Mock
    private ReservableItemCustomRepository reservableItemCustomRepository;
    @Mock
    private ReservableItem reservableItem;
    @InjectMocks
    private ReservableItemMessageEventService reservableItemMessageEventService;

    @Test
    @DisplayName("consumeVerifyReservableItemEvent() - valid request - succeed")
    void should_succeed_when_consumeVerifyReservableItemEvent() {
        // given
        long reservableItemId = 1L;
        long reservableTimeId = 1L;
        long requestQuantity = 10L;
        long businessId = 1L;

        CreateReservationEvent requestPayload = CreateReservationEventBuilder.builder()
                .setVerifyReservableItemRequest(
                        reservableItemId,
                        reservableTimeId,
                        businessId,
                        requestQuantity
                )
                .build();

        Message<String, CreateReservationEvent> message = mock(Message.class);
        when(message.payload()).thenReturn(requestPayload);

        when(reservableItemCustomRepository.findByIdAndBusinessIdAndReservableTimeIdOrElseThrow(
                reservableItemId,
                new ForeignKey(businessId),
                reservableTimeId
        )).thenReturn(reservableItem);

        doNothing().when(reservableItem).validateRequestQuantity(requestQuantity);
        when(reservableItem.getId()).thenReturn(reservableItemId);
        when(reservableItem.getPrice()).thenReturn(1000L);
        when(reservableItem.getMaxQuantityPerUser()).thenReturn(5L);
        when(reservableItem.isTimeAvailable(reservableTimeId)).thenReturn(true);

        // when
        Message<String, CreateReservationEvent> response = reservableItemMessageEventService.consumeVerifyReservableItemEvent(message);

        // then
        verify(reservableItemCustomRepository).findByIdAndBusinessIdAndReservableTimeIdOrElseThrow(reservableItemId, new ForeignKey(businessId), reservableTimeId);
        verify(reservableItem).validateRequestQuantity(requestQuantity);
        assertEquals(Message.Status.RESPONSE_SUCCESS, response.status(), "Response status should be 'RESPONSE_SUCCESS'");
    }

    @Test
    @DisplayName("consumeVerifyReservableItemEvent() - item not available - throw CustomException")
    void should_throwCustomException_when_itemNotAvailable() {
        // given
        long reservableItemId = 1L;
        long reservableTimeId = 1L;
        long requestQuantity = 10L;
        long businessId = 1L;

        CreateReservationEvent requestPayload = CreateReservationEventBuilder.builder()
                .setVerifyReservableItemRequest(
                        reservableItemId,
                        reservableTimeId,
                        businessId,
                        requestQuantity
                )
                .build();

        Message<String, CreateReservationEvent> message = mock(Message.class);
        when(message.payload()).thenReturn(requestPayload);

        when(reservableItemCustomRepository.findByIdAndBusinessIdAndReservableTimeIdOrElseThrow(
                reservableItemId,
                new ForeignKey(businessId),
                reservableTimeId
        )).thenReturn(reservableItem);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                reservableItemMessageEventService.consumeVerifyReservableItemEvent(message));

        // then
        assertEquals(ErrorCode.RESERVABLE_ITEM_IS_NOT_AVAILABLE, exception.getErrorCode(), "Error code should be 'RESERVABLE_ITEM_IS_NOT_AVAILABLE");
    }

    @Test
    @DisplayName("consumeUpdateReservableItemQuantityEvent() - increase quantity - succeed")
    void should_succeed_when_increaseQuantity() {
        // given
        long reservableItemId = 1L;
        long reservableTimeId = 1L;
        long requestQuantity = 10L;

        CreateReservationEvent requestPayload = CreateReservationEventBuilder.builder()
                .setUpdateReservableItemQuantityRequest(
                        reservableItemId,
                        reservableTimeId,
                        requestQuantity
                )
                .build();

        Message<String, CreateReservationEvent> message = mock(Message.class);
        when(message.payload()).thenReturn(requestPayload);

        when(reservableItemCustomRepository.findByIdAndReservableTimeIdOrElseThrow(
                eq(reservableItemId),
                eq(reservableTimeId)
        )).thenReturn(reservableItem);

        // when
        Message<String, CreateReservationEvent> response = reservableItemMessageEventService.consumeUpdateReservableItemQuantityEvent(message);

        // then
        verify(reservableItemCustomRepository).findByIdAndReservableTimeIdOrElseThrow(reservableItemId, reservableTimeId);
        verify(reservableItem).increaseReservableTimeStock(reservableTimeId, requestQuantity);
        assertEquals(Message.Status.RESPONSE_SUCCESS, response.status(), "Response status should be 'RESPONSE_SUCCESS'");
    }

    @Test
    @DisplayName("consumeUpdateReservableItemQuantityEvent() - decrease quantity - succeed")
    void should_succeed_when_decreaseQuantity() {
        // given
        long reservableItemId = 1L;
        long reservableTimeId = 1L;
        long requestQuantity = -10L;

        CreateReservationEvent requestPayload = CreateReservationEventBuilder.builder()
                .setUpdateReservableItemQuantityRequest(
                        reservableItemId,
                        reservableTimeId,
                        requestQuantity
                )
                .build();

        Message<String, CreateReservationEvent> message = mock(Message.class);
        when(message.payload()).thenReturn(requestPayload);

        when(reservableItemCustomRepository.findByIdAndReservableTimeIdOrElseThrow(
                reservableItemId,
                reservableTimeId
        )).thenReturn(reservableItem);

        // when
        Message<String, CreateReservationEvent> response = reservableItemMessageEventService.consumeUpdateReservableItemQuantityEvent(message);

        // then
        verify(reservableItemCustomRepository).findByIdAndReservableTimeIdOrElseThrow(reservableItemId, reservableTimeId);
        verify(reservableItem).decreaseReservableTimeStock(reservableTimeId, -requestQuantity);
        assertEquals(Message.Status.RESPONSE_SUCCESS, response.status(), "Response status should be 'RESPONSE_SUCCESS'");
    }
}