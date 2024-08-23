package org.server.rsaga.reservation.domain;

import io.hypersistence.tsid.TSID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.reservation.domain.constant.ReservationStatus;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reservation Domain Tests")
@ExtendWith(MockitoExtension.class)
class ReservationTest {
    @Nested
    @DisplayName("constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("valid input - Reservation created")
        void should_createReservation_when_validInput() {
            // given
            Long id = TSID.fast().toLong();
            Long businessId = 1L;
            Long userId = 1L;
            Long reservableItemId = 1L;
            Long reservableTimeId = 1L;
            Long quantity = 1L;

            // when
            Reservation reservation = new Reservation(id, businessId, userId, reservableItemId, reservableTimeId, quantity);

            // then
            assertEquals(id, reservation.getId());
            assertEquals(businessId, reservation.getBusinessId());
            assertEquals(userId, reservation.getUserId());
            assertEquals(reservableItemId, reservation.getReservableItemId());
            assertEquals(reservableTimeId, reservation.getReservableTimeId());
            assertEquals(quantity, reservation.getQuantity());
            assertEquals(ReservationStatus.PENDING, reservation.getReservationStatus());
        }

        @Test
        @DisplayName("null input - throws IllegalArgumentException")
        void should_throw_when_nullInput() {
            // given
            Long id = TSID.fast().toLong();

            // when, then
            assertThrows(IllegalArgumentException.class, () -> new Reservation(null, 1L, 1L, 1L, 1L, 1L));
            assertThrows(IllegalArgumentException.class, () -> new Reservation(id, null, 1L, 1L, 1L, 1L));
            assertThrows(IllegalArgumentException.class, () -> new Reservation(id, 1L, null, 1L, 1L, 1L));
            assertThrows(IllegalArgumentException.class, () -> new Reservation(id, 1L, 1L, null, 1L, 1L));
            assertThrows(IllegalArgumentException.class, () -> new Reservation(id, 1L, 1L, 1L, null, 1L));
            assertThrows(IllegalArgumentException.class, () -> new Reservation(id, 1L, 1L, 1L, 1L, null));
        }

        @Test
        @DisplayName("negative quantity - throws IllegalArgumentException")
        void should_throw_when_negativeQuantity() {
            // given
            Long id = TSID.fast().toLong();

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new Reservation(id, 1L, 1L, 1L, 1L, -1L));

            // then
            assertEquals("The quantity must be a positive value.", aThrows.getMessage());
        }

        @Test
        @DisplayName("invalid TSID - throws IllegalArgumentException")
        void should_throw_when_invalidTsid() {
            // given
            Long invalidId = 1L;

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new Reservation(invalidId, 1L, 1L, 1L, 1L, 1L));

            // then
            assertEquals("The id must be in the format TSID.", aThrows.getMessage());
        }
    }

    @Nested
    @DisplayName("updateStatus() tests")
    class UpdateStatusTests {

        @Test
        @DisplayName("Valid status update - succeed")
        void should_succeed_when_validStatusUpdate() {
            // given
            Long id = TSID.fast().toLong();
            Reservation reservation = new Reservation(id, 1L, 1L, 1L, 1L, 1L);

            // when
            reservation.updateStatus(ReservationStatus.RESERVED);

            // then
            assertEquals(ReservationStatus.RESERVED, reservation.getReservationStatus());
        }

        @Test
        @DisplayName("Invalid status update - throws IllegalArgumentException")
        void should_throw_when_invalidStatusUpdate() {
            // given
            Long id = TSID.fast().toLong();
            Reservation reservation = new Reservation(id, 1L, 1L, 1L, 1L, 1L);

            // when
            reservation.updateStatus(ReservationStatus.CANCELED);
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> reservation.updateStatus(ReservationStatus.RESERVED));

            // then
            assertEquals("Changes can only be made when an reservation is in the 'RESERVED' or 'PENDING' state.", aThrows.getMessage());
        }
    }
}