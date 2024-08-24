package org.server.rsaga.reservableitem.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.server.rsaga.reservableitem.domain.constant.Unit;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Stock value tests")
class StockTest {

    @Nested
    @DisplayName("Stock constructor")
    class CreateStock {

        @Test
        @DisplayName("invalid quantity - throw")
        void should_throw_when_invalidQuantity() {
            // given

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new Stock(-1L, Unit.BOX));

            // then
            assertEquals(aThrows.getMessage(), "Stock quantities should not be a negative value.");
        }

        @Test
        @DisplayName("invalid unit - throw")
        void should_throw_when_invalidUnit() {
            // given

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> new Stock(1L, null));

            // then
            assertEquals(aThrows.getMessage(), "Stock units should not be null.");
        }
    }

    @Test
    @DisplayName("increaseQuantity() - valid parameter - increased")
    void should_succeedIncreaseQuantity_when_validParameter() {
        // given
        Stock stock = new Stock(1L, Unit.BOX);

        // when
        Stock increasedStock = stock.increaseQuantity(10L);

        // then
        assertNotSame(stock, increasedStock, "The result should be a new Money instance.");
        assertEquals(11L, increasedStock.getQuantity(), "The quantity should be 11L.");
    }

    @Nested
    @DisplayName("decreaseQuantity()")
    class DecreaseStockQuantity {
        @Test
        @DisplayName("valid parameter - succeed")
        void should_succeed_when_validParameter() {
            // given
            Stock stock = new Stock(11L, Unit.BOX);

            // when
            Stock decreasedStock = stock.decreaseQuantity(1L);

            // then
            assertNotSame(stock, decreasedStock, "The result should be a new Money instance.");
            assertEquals(10L, decreasedStock.getQuantity(), "The quantity should be 11L.");
        }

        @Test
        @DisplayName("insufficient stock - throw")
        void should_throw_when_insufficientStock() {
            // given
            Stock stock = new Stock(1L, Unit.BOX);
            Long decrement = 2L;

            // when
            IllegalArgumentException aThrows = assertThrows(IllegalArgumentException.class, () -> stock.decreaseQuantity(decrement));

            // then
            Unit unit = stock.getUnit();
            assertEquals(aThrows.getMessage(), "Insufficient stock available to decrease by " + decrement + " " + unit.name());
            assertEquals(1L, stock.getQuantity(), "The quantity should be 1L.");
        }
    }
}