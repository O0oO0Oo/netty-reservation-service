package org.server.rsaga.payment.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(ForeignKey userId);

    Optional<Payment> findByReservationIdAndUserId(ForeignKey reservationId, ForeignKey userId);
}
