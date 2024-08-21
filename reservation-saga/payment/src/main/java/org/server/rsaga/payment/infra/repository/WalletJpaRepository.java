package org.server.rsaga.payment.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.payment.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletJpaRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByIdAndUserId(Long id, ForeignKey userId);

    Optional<Wallet> findByUserId(ForeignKey userId);
}
