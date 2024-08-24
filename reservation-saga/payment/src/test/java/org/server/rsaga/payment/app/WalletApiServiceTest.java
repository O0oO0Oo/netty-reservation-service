package org.server.rsaga.payment.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.payment.domain.Wallet;
import org.server.rsaga.payment.dto.request.ModifyWalletRequest;
import org.server.rsaga.payment.dto.response.WalletDetailsResponse;
import org.server.rsaga.payment.infra.repository.WalletCustomRepository;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletApiService tests")
class WalletApiServiceTest {
    @Mock
    WalletCustomRepository walletCustomRepository;

    @InjectMocks
    WalletApiService walletApiService;

    Wallet wallet;
    Long walletId;
    Long userId;

    @BeforeEach
    void setUp() {
        walletId = 1L;
        userId = 1L;

        wallet = new Wallet(new ForeignKey(userId), new Money(1000L));
        ReflectionTestUtils.setField(wallet, "id", walletId);
    }

    @Test
    @DisplayName("deposit() - valid request - increase balance")
    void should_increaseBalance_when_depositAndValidRequest() {
        // given
        ModifyWalletRequest request = new ModifyWalletRequest(userId, 500L);
        when(walletCustomRepository.findByIdAndUserIdOrElseThrow(walletId, new ForeignKey(userId)))
                .thenReturn(wallet);

        Money oldBalance = wallet.getBalance();

        // when
        WalletDetailsResponse response = walletApiService.deposit(walletId, request);

        // then
        Money resultBalance = wallet.getBalance();
        assertNotSame(oldBalance, resultBalance);

        assertEquals(walletId, response.walletId());
        assertEquals(userId, response.userId());
        assertEquals(1500L, response.balance());
        assertEquals(1500L, wallet.getBalance().getAmount());
        verify(walletCustomRepository, only()).findByIdAndUserIdOrElseThrow(walletId, new ForeignKey(userId));
    }

    @Test
    @DisplayName("withdraw() - valid request - decrease balance")
    void should_decreaseBalance_when_withdrawAndValidRequest() {
        // given
        ModifyWalletRequest request = new ModifyWalletRequest(userId, 300L);
        when(walletCustomRepository.findByIdAndUserIdOrElseThrow(walletId, new ForeignKey(userId)))
                .thenReturn(wallet);

        Money oldBalance = wallet.getBalance();

        // when
        WalletDetailsResponse response = walletApiService.withdraw(walletId, request);

        // then
        Money resultBalance = wallet.getBalance();
        assertNotSame(oldBalance, resultBalance);

        assertNotNull(response);
        assertEquals(walletId, response.walletId());
        assertEquals(userId, response.userId());
        assertEquals(700L, response.balance());
        assertEquals(700L, wallet.getBalance().getAmount());
        verify(walletCustomRepository, only()).findByIdAndUserIdOrElseThrow(walletId, new ForeignKey(userId));
    }
}