package org.server.rsaga.payment.infra.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.payment.domain.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("WalletJpaRepository tests")
class WalletJpaRepositoryTest {
    @Autowired
    WalletJpaRepository walletJpaRepository;

    @Test
    @DisplayName("findAllByUserIdIn() - succeed.")
    void shouldReturnWalletsWhenUserIdsAreProvided() {
        // given
        long userId1 = -1L;
        long userId2 = -2L;
        Wallet wallet1 = new Wallet(new ForeignKey(userId1), new Money(1000L));
        walletJpaRepository.save(wallet1);

        Wallet wallet2 = new Wallet(new ForeignKey(userId2), new Money(1000L));
        walletJpaRepository.save(wallet2);

        List<Long> userIds = Arrays.asList(userId1, userId2);

        // when
        List<Wallet> wallets = walletJpaRepository.findAllByUserIdIn(userIds);

        // then
        assertThat(wallets)
                .withFailMessage("Expected to find 2 wallets for userIds %s, but found %s", userIds, wallets.size())
                .isNotEmpty()
                .hasSize(2);
        assertThat(wallets)
                .withFailMessage("Expected user ids to be %s, but found %s", userIds, wallets.stream().map(Wallet::getUserId).toList())
                .extracting(Wallet::getUserId)
                .containsExactlyInAnyOrder(userId1, userId2);
    }
}