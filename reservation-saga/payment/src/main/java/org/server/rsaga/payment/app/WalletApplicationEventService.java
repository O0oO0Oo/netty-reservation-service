package org.server.rsaga.payment.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.common.event.CreateWalletEvent;
import org.server.rsaga.payment.domain.Wallet;
import org.server.rsaga.payment.infra.repository.WalletJpaRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletApplicationEventService {
    private final WalletJpaRepository walletJpaRepository;

    @EventListener
    @Transactional
    public void handleCreateWalletEvent(CreateWalletEvent event) {
        Wallet wallet = new Wallet(
                new ForeignKey(event.userId()),
                new Money(0L)
        );
        walletJpaRepository.save(wallet);
    }
}
