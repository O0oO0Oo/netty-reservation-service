package org.server.rsaga.payment.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.payment.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletJpaRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByIdAndUserId(Long id, ForeignKey userId);

    Optional<Wallet> findByUserId(ForeignKey userId);

    @Query("SELECT w FROM Wallet w WHERE w.userId.id IN :userIds")
    List<Wallet> findAllByUserIdIn(@Param("userIds") Iterable<Long> userIds);
}