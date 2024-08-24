package org.server.rsaga.payment.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wallet tests")
class WalletTest {
    Wallet wallet;
    ForeignKey userId;
    Money initialBalance;

    @BeforeEach
    void setUp() {
        userId = new ForeignKey(1L);
        initialBalance = new Money(1000L);
        wallet = new Wallet(userId, initialBalance);
    }

    @Test
    @DisplayName("Wallet constructor - valid parameter - created")
    void should_createWallet_when_validInput() {
        // given
        ForeignKey userId = new ForeignKey(2L);
        Money balance = new Money(500L);

        // when
        Wallet wallet = new Wallet(userId, balance);

        // then
        assertNotNull(wallet);
        assertEquals(userId.getId(), wallet.getUserId());
        Money resultBalance = wallet.getBalance();
        assertEquals(balance.getAmount(), resultBalance.getAmount());
    }

    @Test
    @DisplayName("addBalance() - valid parameter - increased")
    void should_increaseBalance_when_addBalance() {
        // given
        Money increment = new Money(500L);
        Money oldBalance = wallet.getBalance();

        // when
        wallet.addBalance(increment);

        // then
        Money resultBalance = wallet.getBalance();
        assertEquals(1500L, resultBalance.getAmount(), "Balance should increase by " + increment.getAmount());
        assertNotSame(oldBalance, resultBalance);
    }

    @Test
    @DisplayName("addBalance() - null input - not change")
    void should_notChangeBalance_when_addBalanceNull() {
        // given
        Money oldBalance = wallet.getBalance();

        // when
        wallet.addBalance(null);

        // then
        Money resultBalance = wallet.getBalance();
        assertEquals(1000L, resultBalance.getAmount(), "Balance should not be changed");
        assertSame(oldBalance, resultBalance);
    }

    @Test
    @DisplayName("subtractBalance() - valid parameter - decreased")
    void should_decreaseBalance_when_subtractBalance() {
        // given
        Money decrement = new Money(300L);

        // when
        wallet.subtractBalance(decrement);

        // then
        Money resultBalance = wallet.getBalance();
        assertEquals(700L, resultBalance.getAmount(), "Balance should decrease by " + decrement.getAmount());
    }

    @Test
    @DisplayName("subtractBalance() - null input - not change")
    void should_notChangeBalance_when_subtractBalanceWithNull() {
        // given
        Money oldBalance = wallet.getBalance();

        // when
        wallet.subtractBalance(null);

        // then
        Money resultBalance = wallet.getBalance();
        assertEquals(1000L, resultBalance.getAmount(), "Balance should not be changed.");
        assertSame(oldBalance, resultBalance);
    }
}