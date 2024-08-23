package org.server.rsaga.reservableitem.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.reservableitem.domain.constant.Unit;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReservableTime tests")
@ExtendWith(MockitoExtension.class)
class ReservableTimeTest {


    @Nested
    @DisplayName("create ReservableTime tests")
    class CreateReservableTime {
        @Test
        @DisplayName("date is past than today - throw")
        void should_throw_when_() {
            // given
            Date pastDate = new Date(System.currentTimeMillis() - 3600 * 1000);
            Stock stock = new Stock(1L, Unit.BOX);

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () ->
                    new ReservableTime(
                            pastDate,
                            stock,
                            true
                    ));

            // then
            assertEquals("The date must be after today.", aThrows.getMessage());
        }


        @Test
        @DisplayName("invalid null parameter - throw")
        void should_throw_when_stateUnderTest() {
            // given
            Date futureDate = new Date(System.currentTimeMillis() + 3600 * 1000);
            Stock stock = new Stock(1L, Unit.BOX);

            // when
            IllegalArgumentException aThrows1 = assertThrows(IllegalArgumentException.class, () ->
                    new ReservableTime(
                            null,
                            stock,
                            true
                    ));
            IllegalArgumentException aThrows2 = assertThrows(IllegalArgumentException.class, () ->
                    new ReservableTime(
                            futureDate,
                            null,
                            true
                    ));

            // then
            assertEquals("The reservableTime must not be null.", aThrows1.getMessage());
            assertEquals("The stock must not be null.", aThrows2.getMessage());

        }
    }

    @Test
    @DisplayName("adjustStock() - valid parameter - new value is assigned")
    void should_newValueAssigned_when_adjustStock() {
        // given
        Date date = new Date(System.currentTimeMillis() + 3600 * 1000);
        Stock stock = new Stock(1L, Unit.BOX);
        ReservableTime reservableTime = new ReservableTime(
                date,
                stock,
                true
        );

        Long newQuantity = 10L;
        Stock newStock = new Stock(newQuantity, Unit.BOX);
        // when
        reservableTime.adjustStock(newStock);

        // then
        Stock resultStock = reservableTime.getStock();

        assertNotSame(stock, resultStock, "The result should not be same.");
        assertSame(newStock, resultStock, "The result should be same.");
        assertEquals(newQuantity, resultStock.getQuantity(), "The amount should be 10L.");
    }

    @Test
    @DisplayName("rescheduleDate() - valid date - new date is assigned")
    void should_newDateAssigned_when_rescheduleDate() {
        // given
        Date date = new Date(System.currentTimeMillis() + 3600 * 1000);
        Stock stock = new Stock(1L, Unit.BOX);
        ReservableTime reservableTime = new ReservableTime(
                date,
                stock,
                true
        );

        Date newDate = new Date(System.currentTimeMillis() + 3600 * 2000);
        // when
        reservableTime.rescheduleDate(newDate);

        // then
        Date resultTime = reservableTime.getTime();
        assertNotEquals(date, resultTime, "The result should not equal.");
        assertEquals(newDate, resultTime, "The result should equal.");
    }
}