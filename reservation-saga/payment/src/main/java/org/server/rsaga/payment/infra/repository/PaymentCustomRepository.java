package org.server.rsaga.payment.infra.repository;

import org.server.rsaga.common.domain.ForeignKey;
import org.server.rsaga.payment.domain.Payment;

public interface PaymentCustomRepository {
    Payment findByReservationIdAndUserId(ForeignKey reservationId, ForeignKey userId);
}
