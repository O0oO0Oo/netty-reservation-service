package org.server.rsaga.payment.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.payment.domain.Wallet;

public interface WalletCustomRepository {
    Wallet findByIdAndUserIdOrElseThrow(Long walletId, ForeignKey userId);
    Wallet findByUserIdOrElseThrow(ForeignKey userId);
}
