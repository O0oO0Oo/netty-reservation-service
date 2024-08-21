package org.server.rsaga.payment.app;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.domain.Money;
import org.server.rsaga.payment.domain.Wallet;
import org.server.rsaga.payment.dto.request.FindWalletRequest;
import org.server.rsaga.payment.dto.request.ModifyWalletRequest;
import org.server.rsaga.payment.dto.response.WalletDetailsResponse;
import org.server.rsaga.payment.infra.repository.WalletCustomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletApiService {
    private final WalletCustomRepository walletCustomRepository;

    @Transactional(readOnly = true)
    public WalletDetailsResponse findWallet(Long walletId, FindWalletRequest request) {
        return WalletDetailsResponse.of(
                walletCustomRepository.findByIdAndUserIdOrElseThrow(walletId,
                        new ForeignKey(request.userId())
                )
        );
    }

    @Transactional
    public WalletDetailsResponse deposit(Long walletId, ModifyWalletRequest request) {
        Wallet wallet = walletCustomRepository.findByIdAndUserIdOrElseThrow(walletId,
                new ForeignKey(request.userId())
        );

        wallet.addBalance(
                new Money(request.amount())
        );

        return WalletDetailsResponse.of(wallet);
    }

    @Transactional
    public WalletDetailsResponse withdraw(Long walletId, ModifyWalletRequest request) {
        Wallet wallet = walletCustomRepository.findByIdAndUserIdOrElseThrow(walletId,
                new ForeignKey(request.userId())
        );

        wallet.subtractBalance(
                new Money(request.amount())
        );

        return WalletDetailsResponse.of(wallet);
    }
}
