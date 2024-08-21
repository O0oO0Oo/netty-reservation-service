package org.server.rsaga.payment.infra.repository.impl;

import lombok.RequiredArgsConstructor;
import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.common.exception.CustomException;
import org.server.rsaga.common.exception.ErrorCode;
import org.server.rsaga.payment.domain.Wallet;
import org.server.rsaga.payment.infra.repository.WalletCustomRepository;
import org.server.rsaga.payment.infra.repository.WalletJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WalletCustomRepositoryImpl implements WalletCustomRepository {
    private final WalletJpaRepository walletJpaRepository;

    @Override
    public Wallet findByIdAndUserIdOrElseThrow(Long walletId, ForeignKey userId) {
        return walletJpaRepository.findByIdAndUserId(
                walletId, userId
        ).orElseThrow(
                () -> new CustomException(ErrorCode.WALLET_NOT_FOUND)
        );
    }

    @Override
    public Wallet findByUserIdOrElseThrow(ForeignKey userId) {
        return walletJpaRepository.findByUserId(
                userId
        ).orElseThrow(
                () -> new CustomException(ErrorCode.WALLET_NOT_FOUND)
        );
    }
}
