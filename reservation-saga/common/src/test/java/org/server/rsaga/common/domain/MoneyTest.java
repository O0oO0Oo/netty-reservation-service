package org.server.rsaga.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money value tests")
class MoneyTest {

    @Nested
    @DisplayName("Money constructor")
    class CreateMoney {
        @Test
        @DisplayName("negative amount - throw")
        void should_throw_when_createNegativeValue() {
            // given

            // when
            IllegalArgumentException aThrows = assertThrows(
                    IllegalArgumentException.class, () -> new Money(-1L)
            );

            // then
            assertEquals("The money amount should be a negative value.", aThrows.getMessage());
        }

        @Test
        @DisplayName("valid amount - created")
        void should_created_when_create() {
            // given

            // when
            Money money = new Money(1L);

            // then
            assertEquals(1L, money.getAmount(), "The amount should be 1.");
        }
    }

    @Test
    @DisplayName("add() - valid value - added")
    void should_added_when_addMoney() {
        // given
        Money money1 = new Money(1L);
        Money money2 = new Money(2L);

        // when
        Money result = money1.add(money2);

        // then
        assertEquals(3L, result.getAmount(), "The total amount should be 3.");
        assertNotSame(money1, result, "The result should be a new Money instance.");
    }

    @Test
    @DisplayName("subtract() - valid value - subtracted")
    void should_subtracted_when_subtractMoney() {
        // given
        Money money1 = new Money(2L);
        Money money2 = new Money(1L);

        // when
        Money result = money1.subtract(money2);

        // then
        assertEquals(1L, result.getAmount(), "The total amount should be 3.");
        assertNotSame(money1, result, "The result should be a new Money instance.");
    }
}