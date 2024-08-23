package org.server.rsaga.reservableitem.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.common.event.BusinessValidationEvent;
import org.server.rsaga.common.event.EventPublisher;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ReservableItem tests")
@ExtendWith(MockitoExtension.class)
class ReservableItemTest {
    String name;
    Long maxQuantityPerUser;
    @Mock
    List<ReservableTime> reservableTimes;
    Money price;
    ForeignKey businessId;
    boolean isItemAvailable;

    ReservableItem testReservableItem;

    @BeforeEach
    void setUp() {
        name = "test item";
        maxQuantityPerUser = 5L;
        reservableTimes = new ArrayList<>();
        price = new Money(1000L);
        businessId = new ForeignKey(1L);
        isItemAvailable = false;

        for (int i = 0; i < 3; i++) {
            reservableTimes.add(mock(ReservableTime.class));
        }

        try (MockedStatic<EventPublisher> eventPublisherMockedStatic = mockStatic(EventPublisher.class)) {
            BusinessValidationEvent businessValidationEvent = new BusinessValidationEvent(businessId.getId());
            eventPublisherMockedStatic
                    .when(
                            () -> EventPublisher.publish(businessValidationEvent)
                    )
                    .thenAnswer(invocation -> null);

            testReservableItem = new ReservableItem(
                    name, maxQuantityPerUser, reservableTimes, price, businessId, isItemAvailable
            );
        }
    }

    @Nested
    @DisplayName("create ReservableItem tests")
    class CreateReservableItem {
        @Test
        @DisplayName("invalid name - throw")
        void should_throw_when_invalidName() {
            // given

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new ReservableItem(
                    null,
                    maxQuantityPerUser,
                    reservableTimes,
                    price,
                    businessId,
                    isItemAvailable
            ));

            // then
            assertEquals("Reservable-Item name cannot be empty.", aThrows.getMessage());
        }

        @Test
        @DisplayName("invalid maxQuantityPerUser - throw")
        void should_throw_when_invalidMaxQuantityPerUser() {
            // given

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new ReservableItem(
                    name,
                    -1L,
                    reservableTimes,
                    price,
                    businessId,
                    isItemAvailable
            ));

            // then
            assertEquals("The maxQuantityPerUser should not be a negative value.", aThrows.getMessage());
        }

        @Test
        @DisplayName("invalid reservableTimes - throw")
        void should_throw_when_invalidReservableTimes() {
            // given

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new ReservableItem(
                    name,
                    maxQuantityPerUser,
                    Collections.emptyList(),
                    price,
                    businessId,
                    isItemAvailable
            ));

            // then
            assertEquals("The list of reservable times should contain at least one reservable time.", aThrows.getMessage());
        }

        @Test
        @DisplayName("invalid price - throw")
        void should_throw_when_invalidPrice() {
            // given

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new ReservableItem(
                    name,
                    maxQuantityPerUser,
                    reservableTimes,
                    null,
                    businessId,
                    isItemAvailable
            ));

            // then
            assertEquals("The price should not be null.", aThrows.getMessage());
        }

        @Test
        @DisplayName("validateBusinessId() - invalid businessId - throw")
        void should_throw_when_validateBusinessIdAndInvalidBusinessId() {
            try (MockedStatic<EventPublisher> eventPublisherMockedStatic = mockStatic(EventPublisher.class)){
                // given
                BusinessValidationEvent businessValidationEvent = new BusinessValidationEvent(businessId.getId());
                eventPublisherMockedStatic.when(
                                () -> EventPublisher.publish(businessValidationEvent)
                        )
                        .thenThrow(new CustomException(ErrorCode.BUSINESS_NOT_FOUND));

                // when
                CustomException customException = assertThrows(CustomException.class, () -> new ReservableItem(
                        name,
                        maxQuantityPerUser,
                        reservableTimes,
                        price,
                        businessId,
                        isItemAvailable
                ));

                // then
                eventPublisherMockedStatic.verify(
                        () -> EventPublisher.publish(businessValidationEvent),
                        only().description("The publish() method should be executed once")
                );
                assertEquals(ErrorCode.BUSINESS_NOT_FOUND, customException.getErrorCode(), "The ErrorCode should be 'BUSINESS_NOT_FOUND'");
            }
        }
    }

    @Test
    @DisplayName("changeName() - invalid name - not changed")
    void should_notChanged_when_invalidNameAndChangeName() {
        // given
        String newName = "";

        // when
        testReservableItem.changeName(newName);

        // then
        String resultName = testReservableItem.getName();
        assertNotEquals(newName, resultName, "The name should not equal.");
        assertEquals(name, resultName, "The result name should equal.");
    }

    @Test
    @DisplayName("increaseReservableTimeStock() - non-exist ReservableTimeId - throw")
    void should_throw_when_nonExistTimeIdAndIncreaseReservableTimeStock() {
        // given
        Long timeId = 1L;
        when(reservableTimes.get(0).getId()).thenReturn(2L);
        when(reservableTimes.get(1).getId()).thenReturn(2L);
        when(reservableTimes.get(2).getId()).thenReturn(2L);

        // when
        IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> testReservableItem.increaseReservableTimeStock(timeId, 10L));

        // then
        assertEquals("ReservableTime not found with id: " + timeId, aThrows.getMessage());
    }

    @Test
    @DisplayName("increaseReservableTimeStock() - exist ReservableTimeId - changed")
    void should_changed_when_existTimeIdAndIncreaseReservableTimeStock() {
        // given
        Long timeId = 1L;
        Long requestQuantity = 10L;
        when(reservableTimes.get(0).getId()).thenReturn(2L);
        when(reservableTimes.get(1).getId()).thenReturn(2L);
        when(reservableTimes.get(2).getId()).thenReturn(timeId);

        ReservableTime reservableTime = reservableTimes.get(2);
        // when
        assertDoesNotThrow(() -> testReservableItem.increaseReservableTimeStock(timeId, requestQuantity));

        // then
        verify(reservableTime, times(1).description("The increaseStock method should be executed once.")).increaseStock(any());
    }

    @Test
    @DisplayName("decreaseReservableTimeStock() - non-exist ReservableTimeId - throw")
    void should_throw_when_nonExistTimeIdAndDecreaseReservableTimeStock() {
        // given
        Long timeId = 1L;
        when(reservableTimes.get(0).getId()).thenReturn(2L);
        when(reservableTimes.get(1).getId()).thenReturn(2L);
        when(reservableTimes.get(2).getId()).thenReturn(2L);

        // when
        IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> testReservableItem.decreaseReservableTimeStock(timeId, 10L));

        // then
        assertEquals("ReservableTime not found with id: " + timeId, aThrows.getMessage());
    }

    @Test
    @DisplayName("decreaseReservableTimeStock() - exist ReservableTimeId - changed")
    void should_changed_when_existTimeIdAndDecreaseReservableTimeStock() {
        // given
        Long timeId = 1L;
        Long requestQuantity = 10L;
        when(reservableTimes.get(0).getId()).thenReturn(2L);
        when(reservableTimes.get(1).getId()).thenReturn(2L);
        when(reservableTimes.get(2).getId()).thenReturn(timeId);

        ReservableTime reservableTime = reservableTimes.get(2);
        // when
        assertDoesNotThrow(() -> testReservableItem.decreaseReservableTimeStock(timeId, requestQuantity));

        // then
        verify(reservableTime, times(1).description("The decreaseStock method should be executed once.")).decreaseStock(any());
    }
}