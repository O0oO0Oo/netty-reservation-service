package org.server.rsaga.payment.dto.response;

import org.server.rsaga.payment.domain.Wallet;

public record WalletDetailsResponse(
        long walletId,
        long userId,
        long balance
) {
    public static WalletDetailsResponse of(Wallet wallet) {
        return new WalletDetailsResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance()
        );
    }
}
